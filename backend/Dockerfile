# ===== Build base =====
FROM python:3.13-slim AS base
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_DISABLE_PIP_VERSION_CHECK=on \
    PIP_NO_CACHE_DIR=on
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential curl \
 && rm -rf /var/lib/apt/lists/*

# ===== Instala deps =====
COPY pyproject.toml* poetry.lock* requirements.txt* ./
# 1) Poetry si hay lock
RUN if [ -f "poetry.lock" ]; then \
      pip install --no-cache-dir poetry && \
      poetry config virtualenvs.create false && \
      poetry install --no-interaction --no-ansi --only main; \
    elif [ -f "requirements.txt" ]; then \
      pip install --no-cache-dir -r requirements.txt; \
    else \
      pip install --no-cache-dir fastapi uvicorn sqlalchemy asyncpg email-validator; \
    fi

# ===== Runtime =====
FROM python:3.13-slim AS runtime
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1
WORKDIR /app

COPY --from=base /usr/local/lib/python3.13 /usr/local/lib/python3.13
COPY --from=base /usr/local/bin /usr/local/bin

COPY . /app

ENV UVICORN_HOST=0.0.0.0 \
    UVICORN_PORT=8000 \
    UVICORN_WORKERS=2
EXPOSE 8000

HEALTHCHECK --interval=30s --timeout=5s --retries=5 \
  CMD curl -fsS http://localhost:${UVICORN_PORT}/health || exit 1

CMD ["sh","-c","uvicorn app:app --host ${UVICORN_HOST} --port ${UVICORN_PORT} --workers ${UVICORN_WORKERS}"]