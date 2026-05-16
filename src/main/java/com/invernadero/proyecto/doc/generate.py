#!/usr/bin/env python3
"""
generate.py — Universal backend generator from SIGMA meta-model JSON.md

Zero-dependency, single-file script that reads JSON.md and generates a complete
backend for multiple frameworks.

Usage:
    python generate.py --target fastapi  --output ./sigma-api
    python generate.py --target express  --output ./sigma-api
    python generate.py --target fastapi  --output ./sigma-api --only entities,dtos
    python generate.py --target express  --json ./path/to/JSON.md --output ./sigma-api

Layers (--only comma-separated):
  entities      Models/entities with fields, types, relationships
  dtos          Request/Response DTOs with validations
  repositories  Data access layer with CRUD + custom queries
  services      Business logic services with TODO markers for business rules
  controllers   REST endpoints with HTTP method, path, roles, params
  middleware    JWT auth guard + global error handler
  config        App entry point, DB config, .env, dependencies

Targets:
  fastapi       Python FastAPI + SQLAlchemy (async)
  express       Node.js Express + Prisma (TypeScript)

For full documentation see: doc-generate.md
"""

import json, os, re, argparse
from datetime import datetime

# =============================================================================
# 1) T Y P E   M A P S
# =============================================================================

_JAVA_TO_PYTHON = {
    "Long": "int", "String": "str", "Integer": "int", "boolean": "bool",
    "Instant": "datetime", "double": "float", "int": "int",
}

_JAVA_TO_TS = {
    "Long": "number", "String": "string", "Integer": "number", "boolean": "boolean",
    "Instant": "Date", "double": "number", "int": "number",
}

TYPE_MAPS = {"fastapi": _JAVA_TO_PYTHON, "express": _JAVA_TO_TS}

_PYTHON_COL_TYPE = {
    "int": "Integer", "str": "String", "bool": "Boolean",
    "datetime": "DateTime(timezone=True)", "float": "Float",
}

_FK_ACTION = {"CASCADE": "CASCADE", "SET_NULL": "SET NULL", "RESTRICT": "RESTRICT"}

# =============================================================================
# 2) N A M I N G   H E L P E R S
# =============================================================================

def _snake(name):
    s = re.sub(r'(?<=[a-z])(?=[A-Z])', '_', name).lower()
    return s

def _pascal(name):
    return name[0].upper() + name[1:] if name else name

def _camel(name):
    return name[0].lower() + name[1:] if name else name

def _plural(name):
    if name.endswith('s'):
        return name + 'es' if name.endswith(('s', 'x', 'z', 'ch', 'sh')) else name
    if name.endswith('y') and len(name) > 2 and name[-2] not in 'aeiou':
        return name[:-1] + 'ies'
    return name + 's'

# =============================================================================
# 3) J S O N   R E A D E R
# =============================================================================

def read_json_md(path="JSON.md"):
    with open(path, "r", encoding="utf-8") as f:
        text = f.read()
    m = re.search(r'```json\s*\n(.*?)```', text, re.DOTALL)
    if not m:
        raise ValueError("No JSON block found in file")
    return json.loads(m.group(1))

# =============================================================================
# 4) F I L E   W R I T E R   H E L P E R
# =============================================================================

def _write(path, content):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"  wrote {path}")

def _fmt_type(raw, target, enum_map=None):
    t = raw.replace("Enum(", "").replace(")", "")
    if t in ("ADMIN", "OPERATOR", "VIEWER") or (enum_map and t in enum_map):
        if target == "fastapi":
            return "str"
        return "string"
    m = TYPE_MAPS.get(target, _JAVA_TO_TS)
    return m.get(t, t)

def _resolve_ref(ref, data):
    for r in data.get("dtos", {}).get("responses", []):
        if r["name"] == ref:
            return r
    for r in data.get("dtos", {}).get("requests", []):
        if r["name"] == ref:
            return r
    return None

# =============================================================================
# 5) G E N E R A T O R S
# =============================================================================
# each generator receives (data, target, output_root) and writes files

def generate_entities(data, target, root):
    entities = data.get("entities", [])
    rels = data.get("relationships", {}).get("summary", [])
    enums = {e["name"]: e for e in data.get("enums", [])}

    if target == "fastapi":
        _write(f"{root}/app/models/__init__.py", "")
        _write(f"{root}/app/models/base.py", """\
from sqlalchemy.orm import DeclarativeBase

class Base(DeclarativeBase):
    pass
""")
        for ent in entities:
            fields_lines = []
            rel_lines = []
            use_fk = False
            use_rel = False

            for f in ent.get("fields", []):
                if f.get("pk"):
                    continue
                py_type = _fmt_type(f["type"], "fastapi", enums)
                col_type = _PYTHON_COL_TYPE.get(py_type, "String")
                nullable = "nullable=True" if f.get("nullable", True) else "nullable=False"
                col_opts = [nullable]
                if f.get("unique"):
                    col_opts.append("unique=True")
                if "columnName" in f:
                    col_opts.append(f"name='{f['columnName']}'")
                if f.get("defaultValue") is not None:
                    val = f['defaultValue']
                    if isinstance(val, str) and val.lower() == "true":
                        val = "True"
                    elif isinstance(val, str) and val.lower() == "false":
                        val = "False"
                    col_opts.append(f"default={val}")
                col_str = ", ".join(col_opts)
                fields_lines.append(f"    {f['name']} = Column({col_type}, {col_str})")

            for r in rels:
                if r["from"] == ent["name"] and r["type"] == "OneToMany":
                    target_ent = r["to"]
                    rel_lines.append(f"    {r['via']} = relationship('{target_ent}', back_populates='{_snake(ent['name'])}')")
                    use_rel = True

                if r["from"] == ent["name"] and r["type"] == "ManyToOne":
                    fk_col = r["via"]
                    target_ent = r["to"]
                    nullable = "nullable=True" if r.get("nullable", True) else "nullable=False"
                    fields_lines.insert(0, f"    {fk_col} = Column(Integer, ForeignKey('{_plural(_snake(target_ent))}.id'), {nullable})")
                    rel_lines.append(f"    {_snake(target_ent)} = relationship('{target_ent}', back_populates='{_plural(_snake(ent['name']))}')")
                    use_fk = True
                    use_rel = True

                if r["to"] == ent["name"] and r["type"] == "ManyToOne" and r["from"] != ent["name"]:
                    source_ent = r["from"]
                    rel_name = _plural(_snake(source_ent))
                    if not any(rel_name in l for l in rel_lines):
                        rel_lines.append(f"    {rel_name} = relationship('{source_ent}', back_populates='{_snake(ent['name'])}')")
                        use_rel = True

            fk_import = "from sqlalchemy import ForeignKey" if use_fk else ""
            rel_import = "from sqlalchemy.orm import relationship" if use_rel else ""
            base_cols = "from sqlalchemy import Column, Integer, String, Boolean, DateTime"

            fields_code = "\n".join(fields_lines)
            rels_code = "\n".join(rel_lines)
            code = f"""\
{base_cols}
{fk_import}
{rel_import}
from app.models.base import Base


class {ent['name']}(Base):
    __tablename__ = '{ent['table']}'

    id = Column(Integer, primary_key=True, index=True)
{fields_code}
{rels_code}
"""
            _write(f"{root}/app/models/{_snake(ent['name'])}.py", code)

        _write(f"{root}/alembic.ini", """\
[alembic]
script_location = alembic
sqlalchemy.url = postgresql+asyncpg://user:pass@localhost:5432/sigma

[loggers]
keys = root,sqlalchemy,alembic

[handlers]
keys = console

[formatters]
keys = generic
""")

    elif target == "express":
        _PRISMA_TYPE_MAP = {
            "Int": "Int", "number": "Int", "String": "String", "string": "String",
            "Boolean": "Boolean", "boolean": "Boolean",
            "DateTime": "DateTime", "Date": "DateTime", "Float": "Float", "float": "Float",
        }

        schema = """\
generator client {
  provider = "prisma-client-js"
}

datasource db {
  provider = "postgresql"
  url      = env("DATABASE_URL")
}

"""
        for ent in entities:
            fields_str = ""
            rel_str = ""

            for f in ent.get("fields", []):
                if f.get("pk"):
                    continue
                ts_type = _fmt_type(f["type"], "express", enums)
                prisma_type = _PRISMA_TYPE_MAP.get(ts_type, "String")
                is_opt = "?" if f.get("nullable", True) else ""
                attr = f['name']
                unique_attr = "  @unique" if f.get("unique") else ""
                fields_str += f"  {attr}  {prisma_type}{is_opt}{unique_attr}\n"

            for r in rels:
                if r["from"] == ent["name"] and r["type"] == "OneToMany":
                    target_ent = r["to"]
                    rel_str += f"  {_plural(_camel(target_ent))}  {target_ent}[]\n"

                if r["from"] == ent["name"] and r["type"] == "ManyToOne":
                    col = r["via"]
                    target_ent = r["to"]
                    nullable = "" if r.get("nullable", True) else ""
                    rel_str += f"  {col}  Int{nullable}\n"
                    rel_str += f"  {_snake(target_ent)}  {target_ent} @relation(fields: [{col}], references: [id])\n"

                if r["to"] == ent["name"] and r["type"] == "ManyToOne" and r["from"] != ent["name"]:
                    source_ent = r["from"]
                    rel_name = _plural(_camel(source_ent))
                    if rel_name not in rel_str:
                        rel_str += f"  {rel_name}  {source_ent}[]\n"

            schema += f"""\
model {ent['name']} {{
  id    Int     @id @default(autoincrement())
{fields_str}{rel_str}
}}
"""
        _write(f"{root}/prisma/schema.prisma", schema)

        # TS models
        _write(f"{root}/src/types/index.ts", "export interface PaginationParams {\n  page?: number;\n  limit?: number;\n}\n")
        for ent in entities:
            lines = []
            for f in ent.get("fields", []):
                if f.get("pk"):
                    continue
                ts_type = _fmt_type(f["type"], "express", enums)
                opt = "?" if f.get("nullable", True) else ""
                lines.append(f"  {_camel(f['name'])}{opt}: {ts_type};")
            rel_lines = []
            for r in rels:
                if r["from"] == ent["name"] and r["type"] == "OneToMany":
                    rel_lines.append(f"  {_plural(_camel(r['to']))}?: {r['to']}[];")
                elif r["to"] == ent["name"] and r["type"] == "ManyToOne":
                    pass
            fields = "\n".join(lines)
            rels_code = "\n".join(rel_lines)
            code = f"""\
export interface {ent['name']} {{
  id: number;
{fields}
{rels_code}
}}
"""
            _write(f"{root}/src/models/{_camel(ent['name'])}.ts", code)


def generate_dtos(data, target, root):
    reqs = data.get("dtos", {}).get("requests", [])
    resps = data.get("dtos", {}).get("responses", [])
    enums = {e["name"]: e for e in data.get("enums", [])}

    if target == "fastapi":
        _write(f"{root}/app/schemas/__init__.py", "")

        # Auth schemas
        _write(f"{root}/app/schemas/auth.py", """\
from pydantic import BaseModel


class AuthRequest(BaseModel):
    email: str
    password: str


class AuthResponse(BaseModel):
    token: str
""")

        for dto in reqs:
            fields = []
            for f in dto.get("fields", []):
                py_type = _fmt_type(f["type"], "fastapi", enums)
                opt = " | None = None" if not f.get("required", True) else ""
                if f.get("validate") and "@NotBlank" in f.get("validate", ""):
                    opt = ""
                fields.append(f"    {_snake(f['name'])}: {py_type}{opt}")

            code = f"""\
from pydantic import BaseModel
from typing import Optional


class {dto['name']}(BaseModel):
{chr(10).join(fields)}
"""
            _write(f"{root}/app/schemas/{_snake(dto['name'])}.py", code)

        for dto in resps:
            if dto.get("fields") is None:
                continue
            fields = []
            for f in dto.get("fields", []):
                py_type = _fmt_type(f["type"], "fastapi", enums)
                ref = f.get("$ref", "")
                if ref and not py_type.startswith("List"):
                    resolved = _resolve_ref(ref.replace("$ref:", "").strip(), data)
                    if resolved:
                        py_type = resolved["name"]
                fields.append(f"    {_snake(f['name'])}: {py_type}")

            code = f"""\
from pydantic import BaseModel
from typing import Optional


class {dto['name']}(BaseModel):
{chr(10).join(fields)}
"""
            _write(f"{root}/app/schemas/{_snake(dto['name'])}.py", code)

    elif target == "express":
        _write(f"{root}/src/dto/index.ts", "// Auto-generated DTOs\n")

        all_dtos = []
        for dto in reqs + resps:
            if dto.get("fields") is None:
                continue
            lines = []
            for f in dto.get("fields", []):
                ts_type = _fmt_type(f["type"], "express", enums)
                ref = f.get("$ref", "")
                if ref:
                    resolved = _resolve_ref(ref.replace("$ref:", "").strip(), data)
                    if resolved:
                        ts_type = resolved["name"]
                if "List<" in str(ts_type) or ts_type.startswith("List"):
                    inner = ts_type.replace("List<", "").replace(">", "")
                    ts_type = f"{inner}[]"
                opt = "?" if not f.get("required", True) else ""
                lines.append(f"  {_camel(f['name'])}{opt}: {ts_type};")
            name = dto["name"]
            all_dtos.append(f"export interface {name} {{\n{chr(10).join(lines)}\n}}")

        code = "\n\n".join(all_dtos)
        _write(f"{root}/src/dto/index.ts", code)


def generate_repositories(data, target, root):
    repos = data.get("repositories", [])
    entities = data.get("entities", [])

    if target == "fastapi":
        _write(f"{root}/app/repositories/__init__.py", "")
        _write(f"{root}/app/repositories/base.py", """\
from sqlalchemy.ext.asyncio import AsyncSession


class BaseRepository:
    def __init__(self, session: AsyncSession):
        self.session = session
""")

        for repo in repos:
            ent_name = repo["entity"]
            ent_snake = _snake(ent_name)
            methods = []
            methods.append(f"""\
    async def find_all(self) -> list[{ent_name}]:
        result = await self.session.execute(select({ent_name}))
        return list(result.scalars().all())

    async def find_by_id(self, id: int) -> {ent_name} | None:
        return await self.session.get({ent_name}, id)

    async def create(self, entity: {ent_name}) -> {ent_name}:
        self.session.add(entity)
        await self.session.commit()
        await self.session.refresh(entity)
        return entity

    async def delete(self, id: int) -> bool:
        obj = await self.find_by_id(id)
        if obj is None:
            return False
        await self.session.delete(obj)
        await self.session.commit()
        return True
""")
            for q in repo.get("customQueries", []):
                q_name = q["method"]
                params = ", ".join(f"{p['name']}: {_fmt_type(p['type'], 'fastapi')}" for p in q.get("params", []))
                methods.append(f"""\
    async def {_snake(q_name)}(self, {params}):
        # TODO: implement {q_name} query
        ...
""")

            code = f"""\
from sqlalchemy import select
from app.repositories.base import BaseRepository
from app.models.{ent_snake} import {ent_name}


class {ent_name}Repository(BaseRepository):
{chr(10).join(methods)}
"""
            _write(f"{root}/app/repositories/{ent_snake}_repository.py", code)

    elif target == "express":
        _write(f"{root}/src/repositories/index.ts", """\
import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

export default prisma;
""")

        for repo in repos:
            ent_name = repo["entity"]
            ent_camel = _camel(ent_name)
            ent_snake = _snake(ent_name)
            methods = []
            methods.append(f"""\
  async findAll() {{
    return prisma.{ent_snake}.findMany();
  }}

  async findById(id: number) {{
    return prisma.{ent_snake}.findUnique({{ where: {{ id }} }});
  }}

  async create(data: any) {{
    return prisma.{ent_snake}.create({{ data }});
  }}

  async update(id: number, data: any) {{
    return prisma.{ent_snake}.update({{ where: {{ id }}, data }});
  }}

  async delete(id: number) {{
    return prisma.{ent_snake}.delete({{ where: {{ id }} }});
  }}
""")
            for q in repo.get("customQueries", []):
                q_name = _camel(q["method"])
                params = ", ".join(f"{p['name']}: {_fmt_type(p['type'], 'express')}" for p in q.get("params", []))
                methods.append(f"""\
  async {q_name}({params}) {{
    // TODO: implement {q_name} query
    throw new Error('Not implemented');
  }}
""")

            methods_body = chr(10).join(methods) + "\n}"
            code = f"""\
import prisma from './index';
import {{ {ent_name} }} from '../models/{ent_camel}';


class {ent_name}Repository {{
{methods_body}

export default new {ent_name}Repository();
"""
            _write(f"{root}/src/repositories/{ent_snake}_repository.ts", code)


def generate_services(data, target, root):
    services = data.get("services", [])
    rules = data.get("businessRules", [])
    entities = data.get("entities", [])

    if target == "fastapi":
        _write(f"{root}/app/services/__init__.py", "")

        for svc in services:
            svc_name = svc["name"]
            methods = []
            for m in svc.get("methods", []):
                params = m.get("params", [])
                ret = m.get("returns", "")
                logic = m.get("logic", "")
                param_str = ", ".join(f"{p['name']}: {_fmt_type(p['type'], 'fastapi')}" for p in params)
                if "self" not in param_str:
                    param_str = ", " + param_str if param_str else ""
                methods.append(f"""\
    async def {_camel(m['name'])}(self{param_str}):
        \"\"\"{logic}\"\"\"
        # TODO: implement business logic
        ...
        return None
""")

            # Attach business rules as comments
            rule_comments = ""
            for r in rules:
                domain = r.get("domain", "")
                for rule in r.get("rules", []):
                    if svc_name.lower().replace("service", "") in domain.lower():
                        rule_comments += f"# Rule {rule.get('id', '')}: {rule.get('condition', '')} -> {rule.get('action', '')}\n"

            code = f"""\
{rule_comments}
class {svc_name}:
    def __init__(self, repository):
        self.repository = repository
{chr(10).join(methods)}
"""
            _write(f"{root}/app/services/{_snake(svc_name)}.py", code)

    elif target == "express":
        for svc in services:
            svc_name = svc["name"]
            repo_camel = _camel(svc_name.replace("Service", "Repository"))
            methods = []
            for m in svc.get("methods", []):
                params = m.get("params", [])
                logic = m.get("logic", "")
                param_str = ", ".join(f"{p['name']}: {_fmt_type(p['type'], 'express')}" for p in params)
                ret_comment = f"// -> {m.get('returns', '')}\n    " if m.get("returns") else ""
                methods.append(f"""\
  async {_camel(m['name'])}({param_str}) {{
    {ret_comment}// {logic}
    // TODO: implement business logic
    throw new Error('Not implemented');
  }}
""")

            rule_comments = ""
            for r in rules:
                domain = r.get("domain", "")
                for rule in r.get("rules", []):
                    if svc_name.lower().replace("service", "") in domain.lower():
                        rule_comments += f"// Rule {rule.get('id', '')}: {rule.get('condition', '')} -> {rule.get('action', '')}\n"

            methods_body = chr(10).join(methods) + "\n}"
            code = f"""\
{rule_comments}
class {svc_name} {{
{methods_body}

export default new {svc_name}();
"""
            _write(f"{root}/src/services/{_snake(svc_name)}.ts", code)
            _write(f"{root}/src/services/index.ts", "// Auto-generated services\n")


def generate_controllers(data, target, root):
    endpoints = data.get("api", {}).get("endpoints", [])
    enums = {e["name"]: e for e in data.get("enums", [])}

    if target == "fastapi":
        _write(f"{root}/app/api/__init__.py", "")
        _write(f"{root}/app/api/router.py", """\
from fastapi import APIRouter
from app.api import auth, crops, events, event_types, lots, users, dashboard

api_router = APIRouter()
api_router.include_router(auth.router, prefix="/auth", tags=["Autenticación"])
api_router.include_router(crops.router, prefix="/crops", tags=["Cultivos"])
api_router.include_router(events.router, prefix="/events", tags=["Eventos"])
api_router.include_router(event_types.router, prefix="/event-types", tags=["Tipos de Evento"])
api_router.include_router(lots.router, prefix="/lots", tags=["Lote"])
api_router.include_router(users.router, prefix="/users", tags=["User Management"])
api_router.include_router(dashboard.router, prefix="/dashboard", tags=["Dashboard"])
""")

        grouped = {}
        for ep in endpoints:
            tag = ep.get("tag", "General")
            grouped.setdefault(tag, []).append(ep)

        for tag, eps in grouped.items():
            tag_id = _snake(tag.replace(" ", "_").replace("í", "i").replace("ó", "o"))
            routes = []
            for ep in eps:
                method = ep["method"].lower()
                path = ep["path"]
                # strip prefix
                api_path = path
                for prefix in ["/api/auth", "/api/crops", "/api/events", "/api/event-types", "/api/lots", "/api/users", "/api/dashboard"]:
                    if api_path.startswith(prefix):
                        api_path = api_path[len(prefix):] or "/"
                        break
                name = ep.get("summary", ep["path"]).split(" ")[0].lower()
                roles = ep.get("roles", [])
                role_cond = ""
                if roles and "ANONYMOUS" not in roles:
                    role_list = ", ".join(f"\"{r}\"" for r in roles)
                    role_cond = f", dependencies=[Depends(role_guard([{role_list}]))]"
                req = ""
                resp_type = "dict"
                if ep.get("requestBody"):
                    ref = ep["requestBody"].get("$ref", "")
                    req = f", {_snake(ref)}"
                    resp_type = ref
                if ep.get("response", {}).get("$ref"):
                    resp_type = ep["response"]["$ref"]
                elif ep.get("response", {}).get("type"):
                    raw = ep["response"]["type"]
                    if raw.startswith("List<"):
                        inner = raw.replace("List<", "").replace(">", "")
                        resp_type = f"list[{inner}]"
                    else:
                        resp_type = raw

                path_params = ep.get("pathParams", [])
                query_params = ep.get("queryParams", [])
                func_params = []
                for p in path_params:
                    func_params.append(f"{p['name']}: int")
                for p in query_params:
                    py_t = _fmt_type(p.get("type", "String"), "fastapi", enums)
                    default = " = None" if not p.get("required", False) else ""
                    func_params.append(f"{p['name']}: {py_t}{default}")

                func_sig = ", ".join(func_params)
                route = f"""\
@router.{method}("{api_path}"{role_cond})
async def {_camel(name)}({func_sig}):
    # TODO: implement {ep.get('summary', method)} endpoint
    return {{"message": "Not implemented"}}
"""
                routes.append(route)

            code = f"""\
from fastapi import APIRouter, Depends
from app.middleware.auth import role_guard

router = APIRouter()

{chr(10).join(routes)}
"""
            _write(f"{root}/app/api/{tag_id}.py", code)

    elif target == "express":
        grouped = {}
        for ep in endpoints:
            tag = ep.get("tag", "General")
            grouped.setdefault(tag, []).append(ep)

        _write(f"{root}/src/routes/index.ts", """\
import { Router } from 'express';
import authRoutes from './auth';
import cropRoutes from './crops';
import eventRoutes from './events';
import eventTypeRoutes from './eventTypes';
import lotRoutes from './lots';
import userRoutes from './users';
import dashboardRoutes from './dashboard';

const router = Router();

router.use('/auth', authRoutes);
router.use('/crops', cropRoutes);
router.use('/events', eventRoutes);
router.use('/event-types', eventTypeRoutes);
router.use('/lots', lotRoutes);
router.use('/users', userRoutes);
router.use('/dashboard', dashboardRoutes);

export default router;
""")

        for tag, eps in grouped.items():
            tag_id = _snake(tag.replace(" ", "_").replace("í", "i").replace("ó", "o"))
            routes = []
            for ep in eps:
                method = ep["method"].lower()
                path = ep["path"]
                for prefix in ["/api/auth", "/api/crops", "/api/events", "/api/event-types", "/api/lots", "/api/users", "/api/dashboard"]:
                    if path.startswith(prefix):
                        path = path[len(prefix):] or "/"
                        break
                # Convert express params :id
                expr_path = re.sub(r'\{(\w+)\}', r':\1', path)
                handler_name = _camel(ep.get("summary", "handler").split(" ")[0])
                roles = ep.get("roles", [])
                middleware = ""
                if roles and "ANONYMOUS" not in roles:
                    middleware = ", authorize()"

                path_params = ep.get("pathParams", [])
                query_params = ep.get("queryParams", [])
                param_docs = ""
                for p in path_params:
                    param_docs += f"  // @param req.params.{p['name']}\n"
                for p in query_params:
                    param_docs += f"  // @param req.query.{p['name']}\n"

                route = f"""\
{param_docs}router.{method}('{expr_path}'{middleware}, async (req, res) => {{
  // TODO: implement {ep.get('summary', method)} endpoint
  res.json({{"message": "Not implemented"}});
}});
"""
                routes.append(route)

            code = f"""\
import {{ Router }} from 'express';
import {{ authorize }} from '../middleware/auth';

const router = Router();

{chr(10).join(routes)}

export default router;
"""
            _write(f"{root}/src/routes/{tag_id}.ts", code)

        # Controllers
        _write(f"{root}/src/controllers/index.ts", "// Auto-generated controllers\n")

        for tag, eps in grouped.items():
            tag_id = _snake(tag.replace(" ", "_").replace("í", "i").replace("ó", "o"))
            functions = []
            for ep in eps:
                handler_name = _camel(ep.get("summary", "handler").split(" ")[0])
                path_params = ep.get("pathParams", [])
                query_params = ep.get("queryParams", [])
                param_strs = ["req: any", "res: any", "next: any"]
                functions.append(f"""\
export const {handler_name} = async ({', '.join(param_strs)}) => {{
  // TODO: implement {ep.get('summary', method)}
  res.json({{"message": "Not implemented"}});
}};
""")
            code = "\n".join(functions)
            _write(f"{root}/src/controllers/{tag_id}.ts", code)


def generate_middleware(data, target, root):
    sec = data.get("security", {})

    if target == "fastapi":
        code = """\
from fastapi import Request, HTTPException, Depends
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
import jwt

security = HTTPBearer()


def role_guard(allowed_roles: list[str]):
    async def check_role(request: Request, credentials: HTTPAuthorizationCredentials = Depends(security)):
        try:
            payload = jwt.decode(credentials.credentials, options={"verify_signature": False})
            authorities = payload.get("authorities", [])
            user_role = None
            for auth in authorities:
                for role in allowed_roles:
                    if f"ROLE_{role}" == auth:
                        user_role = role
                        break
            if user_role is None:
                raise HTTPException(status_code=403, detail="Forbidden")
            request.state.user = payload
        except jwt.PyJWTError:
            raise HTTPException(status_code=401, detail="Invalid token")
    return check_role
"""
        _write(f"{root}/app/middleware/auth.py", code)

        code = """\
from fastapi import Request
from fastapi.responses import JSONResponse


class AppException(Exception):
    def __init__(self, message: str, status_code: int = 400):
        self.message = message
        self.status_code = status_code


async def global_error_handler(request: Request, exc: Exception):
    if isinstance(exc, AppException):
        return JSONResponse(
            status_code=exc.status_code,
            content={"message": exc.message, "error": "Business Error"}
        )
    return JSONResponse(
        status_code=500,
        content={"message": "Unexpected error", "error": "Internal Server Error"}
    )
"""
        _write(f"{root}/app/middleware/error_handler.py", code)
        _write(f"{root}/app/middleware/__init__.py", "")

    elif target == "express":
        code = """\
import {{ Request, Response, NextFunction }} from 'express';
import jwt from 'jsonwebtoken';

export const authorize = (roles?: string[]) => {
  return (req: Request, res: Response, next: NextFunction) => {{
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {{
      return res.status(401).json({{ message: 'Unauthorized' }});
    }}
    try {{
      const token = authHeader.substring(7);
      const decoded = jwt.verify(token, process.env.JWT_SECRET || 'secret');
      (req as any).user = decoded;
      if (roles && roles.length > 0) {{
        const authorities = (decoded as any).authorities || [];
        const hasRole = roles.some(r => authorities.includes(`ROLE_${{r}}`));
        if (!hasRole) {{
          return res.status(403).json({{ message: 'Forbidden' }});
        }}
      }}
      next();
    }} catch (err) {{
      return res.status(401).json({{ message: 'Invalid token' }});
    }}
  }};
}};

export const errorHandler = (err: any, req: Request, res: Response, next: NextFunction) => {{
  console.error(err);
  res.status(err.status || 500).json({{
    message: err.message || 'Unexpected error',
    error: err.name || 'Internal Server Error',
  }});
}};
"""
        _write(f"{root}/src/middleware/auth.ts", code)


def generate_config(data, target, root):
    config = data.get("deployment", {})
    env_vars = config.get("environmentVariables", [])
    sec = data.get("security", {})
    db = data.get("database", {})

    if target == "fastapi":
        env_lines = [f"# {v['description']}" for v in env_vars]
        env_lines += [f"{v['name']}={v.get('defaultValue', '')}" for v in env_vars if v.get('defaultValue')]
        env_lines += [f"{v['name']}=" for v in env_vars if not v.get('defaultValue')]
        _write(f"{root}/.env.example", "\n".join(env_lines) + "\n")

        code = f"""\
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    app_name: str = "SIGMA"
    database_url: str = "{db.get('profiles', {}).get('prod', {}).get('urlTemplate', 'postgresql+asyncpg://user:pass@localhost:5432/sigma')}"
    jwt_secret: str = "change-me"
    jwt_algorithm: str = "{sec.get('algorithm', 'HS256')}"
    jwt_expiration_minutes: int = {sec.get('expiration', {}).get('minutes', 300)}
    frontend_url: str = "http://localhost:3000"

    class Config:
        env_file = ".env"


settings = Settings()
"""
        _write(f"{root}/app/config.py", code)

        code = """\
from sqlalchemy.ext.asyncio import create_async_engine, async_sessionmaker, AsyncSession
from app.config import settings

engine = create_async_engine(settings.database_url, echo=True)
async_session = async_sessionmaker(engine, class_=AsyncSession, expire_on_commit=False)


async def get_session() -> AsyncSession:
    async with async_session() as session:
        yield session
"""
        _write(f"{root}/app/database.py", code)

        code = f"""\
from datetime import datetime
import jwt
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api.router import api_router
from app.config import settings
from app.middleware.error_handler import global_error_handler, AppException

app = FastAPI(
    title="SIGMA API",
    description="{data.get('_metadata', {}).get('sourceProject', {}).get('description', 'SIGMA API')}",
    version="{data.get('_metadata', {}).get('sourceProject', {}).get('version', '1.0.0')}",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=[settings.frontend_url],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(api_router, prefix="/api")

app.add_exception_handler(AppException, global_error_handler)
app.add_exception_handler(Exception, global_error_handler)


@app.get("/api/health")
async def health():
    return {{"status": "ok", "timestamp": datetime.utcnow().isoformat()}}
"""
        _write(f"{root}/app/main.py", code)

        _write(f"{root}/requirements.txt", """\
fastapi>=0.110.0
uvicorn[standard]>=0.29.0
sqlalchemy[asyncio]>=2.0.0
asyncpg>=0.29.0
pydantic>=2.0.0
pydantic-settings>=2.0.0
python-jose[cryptography]>=3.3.0
python-multipart>=0.0.9
""")

    elif target == "express":
        env_lines = [f"# {v['description']}" for v in env_vars]
        env_lines += [f"{v['name']}={v.get('defaultValue', '')}" for v in env_vars if v.get('defaultValue')]
        env_lines += [f"{v['name']}=" for v in env_vars if not v.get('defaultValue')]
        _write(f"{root}/.env.example", "\n".join(env_lines) + "\n")
        _write(f"{root}/.env", "\n".join(env_lines) + "\n")

        code = f"""\
import dotenv from 'dotenv';
dotenv.config();

export const config = {{
  port: parseInt(process.env.PORT || '8080'),
  databaseUrl: process.env.DB_URL || '{db.get('profiles', {}).get('prod', {}).get('urlTemplate', 'postgresql://user:pass@localhost:5432/sigma')}',
  jwtSecret: process.env.JWT_SECRET || 'change-me',
  jwtExpirationMs: {sec.get('expiration', {}).get('milliseconds', 18000000)},
  frontendUrl: process.env.FRONTEND_URL || 'http://localhost:3000',
}};
"""
        _write(f"{root}/src/config.ts", code)

        code = f"""\
import express from 'express';
import cors from 'cors';
import routes from './routes/index';
import {{ errorHandler }} from './middleware/auth';
import {{ config }} from './config';

const app = express();

app.use(cors({{ origin: config.frontendUrl }}));
app.use(express.json());

app.use('/api', routes);

app.get('/api/health', (req, res) => {{
  res.json({{ status: 'ok', timestamp: new Date().toISOString() }});
}});

app.use(errorHandler);

app.listen(config.port, () => {{
  console.log(`SIGMA API running on port ${{config.port}}`);
}});

export default app;
"""
        _write(f"{root}/src/app.ts", code)

        code = """\
{
  "compilerOptions": {
    "target": "ES2022",
    "module": "commonjs",
    "lib": ["ES2022"],
    "outDir": "./dist",
    "rootDir": "./src",
    "strict": true,
    "esModuleInterop": true,
    "resolveJsonModule": true,
    "declaration": true,
    "skipLibCheck": true
  },
  "include": ["src/**/*"]
}
"""
        _write(f"{root}/tsconfig.json", code)

        _write(f"{root}/package.json", """\
{
  "name": "sigma-api",
  "version": "1.0.0",
  "description": "SIGMA - Sistema de Gestión de Invernaderos",
  "main": "dist/app.js",
  "scripts": {
    "dev": "ts-node-dev --respawn src/app.ts",
    "build": "tsc",
    "start": "node dist/app.js",
    "prisma:generate": "prisma generate",
    "prisma:migrate": "prisma migrate dev"
  },
  "dependencies": {
    "@prisma/client": "^5.0.0",
    "cors": "^2.8.5",
    "dotenv": "^16.3.0",
    "express": "^4.18.0",
    "jsonwebtoken": "^9.0.0",
    "bcryptjs": "^2.4.3"
  },
  "devDependencies": {
    "@types/cors": "^2.8.0",
    "@types/express": "^4.17.0",
    "@types/jsonwebtoken": "^9.0.0",
    "@types/bcryptjs": "^2.4.0",
    "@types/node": "^20.0.0",
    "prisma": "^5.0.0",
    "ts-node-dev": "^2.0.0",
    "typescript": "^5.0.0"
  }
}
""")


# =============================================================================
# 6) D I S P A T C H E R
# =============================================================================

GENERATORS = {
    "entities":    generate_entities,
    "dtos":        generate_dtos,
    "repositories": generate_repositories,
    "services":    generate_services,
    "controllers": generate_controllers,
    "middleware":  generate_middleware,
    "config":      generate_config,
}

ALL_LAYERS = list(GENERATORS.keys())

TARGETS = {"fastapi": "FastAPI (Python)", "express": "Express (TypeScript)"}

# =============================================================================
# 7) C L I
# =============================================================================

def main():
    parser = argparse.ArgumentParser(
        description="SIGMA Meta-Model Backend Generator",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=f"""\
Available targets: {', '.join(TARGETS.keys())}
Available layers: {', '.join(ALL_LAYERS)}

Examples:
  python generate.py --target fastapi --output ./sigma-api
  python generate.py --target express --output ./sigma-api --only entities,controllers
  python generate.py --target fastapi --json ../path/to/JSON.md --output ./sigma-api
""")
    parser.add_argument("--target", required=True, choices=list(TARGETS.keys()),
                        help="Target framework")
    parser.add_argument("--output", required=True,
                        help="Output directory for generated code")
    parser.add_argument("--json", default="JSON.md",
                        help="Path to JSON.md meta-model file (default: JSON.md)")
    parser.add_argument("--only",
                        help=f"Comma-separated layers to generate: {', '.join(ALL_LAYERS)}")
    args = parser.parse_args()

    print(f"Reading meta-model from {args.json}...")
    data = read_json_md(args.json)
    print(f"Loaded project: {data.get('_metadata', {}).get('sourceProject', {}).get('name', 'SIGMA')}")

    layers = [l.strip() for l in args.only.split(",")] if args.only else ALL_LAYERS
    for layer in layers:
        if layer not in GENERATORS:
            print(f"  [skip] unknown layer '{layer}'")
            continue
        print(f"[{layer}] generating...")
        GENERATORS[layer](data, args.target, args.output)

    print(f"\nDone! Generated {args.target} backend in: {os.path.abspath(args.output)}")
    print(f"Target: {TARGETS[args.target]}")


if __name__ == "__main__":
    main()
