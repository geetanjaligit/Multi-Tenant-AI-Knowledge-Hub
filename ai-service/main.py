# pyrefly: ignore [missing-import]
from fastapi import FastAPI
from pydantic import BaseModel

# 1. Initialize the FastAPI app
app = FastAPI(title="Multi-Tenant AI Knowledge Hub - AI Service")

# 2. Define a simple Data Model (Request Body)
# In Spring Boot terms: This is a DTO (Data Transfer Object)
class ProcessRequest(BaseModel):
    document_id: int
    content: str

# --- New Chunking Logic ---
def chunk_text(text: str, chunk_size: int = 500, overlap: int = 50):
    """
    Chops a long string into smaller pieces (chunks).
    chunk_size: Number of characters per piece.
    overlap: How many characters to repeat from the previous piece.
    """
    chunks = []
    # This is a basic sliding window loop
    for i in range(0, len(text), chunk_size - overlap):
        chunk = text[i : i + chunk_size]
        chunks.append(chunk)
    return chunks

# 3. Basic Health Check Endpoint
@app.get("/")
async def root():
    return {"status": "AI Service is Up and Running"}

# 4. Updated processing endpoint with Chunking
@app.post("/process")
async def process_document(request: ProcessRequest):
    print(f"--- Processing Document {request.document_id} ---")
    
    # Run the chunking logic
    chunks = chunk_text(request.content)
    
    print(f"Created {len(chunks)} chunks for processing.")
    
    # Let's print the first chunk to the console so we can see it
    if chunks:
        print(f"Preview of Chunk 1: {chunks[0][:100]}...")

    return {
        "message": "Document chunked successfully",
        "document_id": request.document_id,
        "total_chunks": len(chunks),
        "chunks": chunks  # Returning the actual list of text chunks
    }
