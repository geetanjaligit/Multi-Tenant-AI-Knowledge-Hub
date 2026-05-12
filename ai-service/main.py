import os
from fastapi import FastAPI
from pydantic import BaseModel
from google import genai
from dotenv import load_dotenv

# Load Environment Variables
load_dotenv()

# Configure Gemini Client (The Modern Way)
api_key = os.getenv("GEMINI_API_KEY")
client = None
if api_key:
    client = genai.Client(api_key=api_key)

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

# 4. Updated processing endpoint with Batch Embeddings
@app.post("/process")
async def process_document(request: ProcessRequest):
    print(f"--- Processing Document {request.document_id} ---")
    
    if not client:
        return {"status": "error", "message": "Gemini API key not configured"}

    try:
        # Step 1: Chunking
        chunks = chunk_text(request.content)
        print(f"Created {len(chunks)} chunks.")

        # Step 2: AI Embedding Generation
        # We use a fallback loop to ensure the app stays running if model names change
        models_to_try = ["models/gemini-embedding-2", "models/gemini-embedding-001"]
        response = None
        used_model = ""

        for model_name in models_to_try:
            try:
                response = client.models.embed_content(
                    model=model_name,
                    contents=chunks,
                    config=genai.types.EmbedContentConfig(task_type="RETRIEVAL_DOCUMENT")
                )
                used_model = model_name
                break 
            except Exception as e:
                error_msg = str(e)
                if "404" in error_msg:
                    continue # Try the next model
                else:
                    raise Exception(f"AI Service Error: {error_msg}")

        if not response:
            raise Exception("No supported embedding models found for your API key.")

        print(f"Successfully generated {len(response.embeddings)} embeddings using {used_model}.")

        # SDK returns embeddings directly in a list
        embeddings = response.embeddings

        # Step 3: Combine text with its embedding
        response_data = []
        for i in range(len(chunks)):
            # Each embedding has a 'values' property in the new SDK
            response_data.append({
                "content": chunks[i],
                "chunkIndex": i,
                "embedding": embeddings[i].values # This is a list of floats
            })

        return {
            "status": "success",
            "document_id": request.document_id,
            "total_chunks": len(chunks),
            "data": response_data
        }

    except Exception as e:
        print(f"Error during AI processing: {str(e)}")
        return {
            "status": "error",
            "message": str(e)
        }
