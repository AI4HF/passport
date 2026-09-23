"""
Generate the ER diagrams of the passport data model from the DDL in docker/deployment/init-db.sql.

    python docs/data-model/generate_er.py

Rewrites docs/data-model/README.md next to this script. Run it after every schema change; the DDL is
the source of truth and the diagrams are never edited by hand.

Every table must belong to exactly one domain in DOMAINS below: a new table fails the run until it is
placed, so the diagrams cannot silently fall behind the schema.
"""

import re
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
DDL = HERE.parent.parent / "docker" / "deployment" / "init-db.sql"
OUT = HERE / "README.md"

# Audit columns point at personnel from almost every table; drawn as relationships they would bury
# the real structure, so they are listed as attributes but not drawn as edges.
AUDIT_COLUMNS = {"created_by", "last_updated_by"}

DOMAINS = [
    ("Study and organization",
     "Who takes part: organizations, their personnel and software agents, the study and its members.",
     ["organization", "personnel", "software_agent", "study", "study_personnel", "study_organization",
      "experiment", "survey"]),
    ("Cohort and features",
     "The declarative definitions executed by Studyfyr, each identified by (url, version).",
     ["population", "featureset", "feature"]),
    ("Datasets and publication",
     "Extracted datasets, their statistics and HealthDCAT-AP publication records.",
     ["dataset", "feature_dataset_characteristic", "dataset_concept", "catalogue_dataset",
      "dataset_distribution", "catalogue_registration"]),
    ("Data quality",
     "Quality criteria definitions and the assessments run against datasets.",
     ["quality_criteria", "quality_criterion", "quality_assessment", "quality_criterion_assessment_result"]),
    ("Training",
     "Learning datasets and their transformations, the learning process, its stages and parameters.",
     ["dataset_transformation", "dataset_transformation_step", "learning_dataset", "parameter", "algorithm",
      "implementation", "learning_process", "learning_stage", "learning_process_dataset",
      "learning_process_parameter", "learning_stage_parameter"]),
    ("Model and evaluations",
     "The model with its lineage, parameters, figures and publications, and its evaluation runs.",
     ["model", "model_parameter", "model_figure", "linked_article", "model_evaluation",
      "model_evaluation_dataset", "evaluation_measure"]),
    ("Passport and audit",
     "The signed, versioned passport and the audit log it freezes.",
     ["passport", "audit_log", "audit_log_book"]),
]

CREATE = re.compile(r"CREATE TABLE\s+(\w+)\s*\((.*?)\n\);", re.S | re.I)
COLUMN = re.compile(r"^(\w+)\s+([A-Za-z]+)(.*)$")
REF = re.compile(r"REFERENCES\s+(\w+)\s*\(\s*(\w+)\s*\)", re.I)
TABLE_KEY = re.compile(r"^(PRIMARY KEY|UNIQUE)\s*\(([^)]*)\)", re.I)


def parse(sql):
    """Return {table: {"columns": [...], "pk": set, "unique": [set, ...]}} in DDL order."""
    ddl = sql.split("INSERT INTO", 1)[0]
    tables = {}
    for name, body in CREATE.findall(ddl):
        columns, pk, unique = [], set(), []
        for raw in body.splitlines():
            line = raw.split("--", 1)[0].strip().rstrip(",")
            if not line:
                continue
            key = TABLE_KEY.match(line)
            if key:
                cols = {c.strip() for c in key.group(2).split(",")}
                (pk.update if key.group(1).upper() == "PRIMARY KEY" else unique.append)(cols)
                continue
            m = COLUMN.match(line)
            if not m:
                sys.exit(f"{name}: cannot parse line {raw.strip()!r}")
            col, typ, rest = m.group(1), m.group(2).lower(), m.group(3)
            ref = REF.search(rest)
            if "PRIMARY KEY" in rest.upper():
                pk.add(col)
            if re.search(r"\bUNIQUE\b", rest, re.I):
                unique.append({col})
            columns.append({
                "name": col, "type": typ, "ref": ref.group(1) if ref else None,
                "not_null": "NOT NULL" in rest.upper(),
            })
        tables[name.lower()] = {"columns": columns, "pk": pk, "unique": unique}
    return tables


def relationships(tables):
    """(parent, child, column, one_to_one) for every drawn foreign key."""
    rels = []
    for child, t in tables.items():
        for c in t["columns"]:
            if c["ref"] and c["name"] not in AUDIT_COLUMNS:
                one_to_one = {c["name"]} in t["unique"] or t["pk"] == {c["name"]}
                required = c["not_null"] or c["name"] in t["pk"]
                rels.append((c["ref"], child, c["name"], one_to_one, required))
    return rels


def edge(parent, child, column, one_to_one, not_null):
    left = "||" if not_null else "|o"
    right = "o|" if one_to_one else "o{"
    return f'    {parent} {left}--{right} {child} : "{column}"'


def attributes(t):
    lines = []
    for c in t["columns"]:
        keys = []
        if c["name"] in t["pk"]:
            keys.append("PK")
        if c["ref"]:
            keys.append("FK")
        if {c["name"]} in t["unique"]:
            keys.append("UK")
        lines.append(f"        {c['type']} {c['name']}" + (f" {', '.join(keys)}" if keys else ""))
    return lines


def diagram(tables, rels, members):
    """
    The erDiagram of one domain. Each foreign key is drawn once, in the domain of the table holding
    it, so a domain shows its own tables with attributes plus, bare, the parents they point to.
    """
    out = ["```mermaid", "erDiagram"]
    for name in members:
        out.append(f"    {name} {{")
        out += attributes(tables[name])
        out.append("    }")
    out += [edge(*r) for r in rels if r[1] in members]
    out.append("```")
    return out


def domain_map(rels):
    """A flowchart of the domains: an arrow from a domain to each domain whose tables reference it."""
    domain_of = {t: i for i, (_, _, members) in enumerate(DOMAINS) for t in members}
    out = ["```mermaid", "flowchart TB"]
    for i, (title, _, members) in enumerate(DOMAINS):
        out.append(f'    d{i}["<b>{title}</b><br/>{len(members)} tables"]')
    links = sorted({(domain_of[parent], domain_of[child]) for parent, child, *_ in rels
                    if domain_of[child] != domain_of[parent]})
    out += [f"    d{a} --> d{b}" for a, b in links]
    out.append("```")
    return out


def main():
    tables = parse(DDL.read_text(encoding="utf-8"))
    placed = [t for _, _, members in DOMAINS for t in members]
    missing = [t for t in tables if t not in placed]
    unknown = [t for t in placed if t not in tables]
    doubled = {t for t in placed if placed.count(t) > 1}
    if missing or unknown or doubled:
        sys.exit(f"DOMAINS is out of date. Not placed: {missing}. Not in the DDL: {unknown}. "
                 f"Placed twice: {sorted(doubled)}.")
    rels = relationships(tables)

    doc = [
        "# Passport data model",
        "",
        "<!-- Generated by generate_er.py from docker/deployment/init-db.sql. Do not edit by hand. -->",
        "",
        f"{len(tables)} tables, generated from the DDL in "
        "[`docker/deployment/init-db.sql`](../../docker/deployment/init-db.sql), which is the source of "
        "truth. After a schema change, regenerate with `python docs/data-model/generate_er.py`.",
        "",
        "- Keys are marked `PK`, `FK` and `UK` (unique). Every identifier is a `varchar` UUID.",
        "- `created_by` and `last_updated_by` reference `personnel` from most tables. They are listed as "
        "attributes but not drawn, so the real structure stays visible.",
        "- Each relationship is drawn once, in the domain of the table holding the foreign key. Tables "
        "from other domains appear there without attributes.",
        "",
        "For how the data model fits the wider system, see the [component overview](../architecture/README.md).",
        "",
        "## Overview",
        "",
        "The seven domains below. An arrow points from a domain to the domains whose tables reference it.",
        "",
        *domain_map(rels),
        "",
    ]
    for title, blurb, members in DOMAINS:
        doc += [f"## {title}", "", blurb, "", *diagram(tables, rels, members), ""]

    OUT.write_text("\n".join(doc), encoding="utf-8", newline="\n")
    print(f"{OUT.name}: {len(tables)} tables, {len(rels)} relationships, {len(DOMAINS)} domains")


if __name__ == "__main__":
    main()
