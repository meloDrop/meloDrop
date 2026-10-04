"""Load the Markdown files that describe the routes in Swagger."""

from pathlib import Path

# goes back to backend/
BASE_DIR = Path(__file__).resolve().parents[2]
DOCS_DIR = BASE_DIR / "docs"


def load_doc(filename: str) -> str:
    """Read a file from backend/docs, or return an empty string if it is missing."""
    file_path = DOCS_DIR / filename

    if not file_path.exists():
        print(f" Warning: The document file '{file_path}' doesn't exist.")
        return ""

    return file_path.read_text(encoding="utf-8")