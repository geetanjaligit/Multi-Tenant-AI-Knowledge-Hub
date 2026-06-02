import os
# pyrefly: ignore [missing-import]
from fastapi import FastAPI
# pyrefly: ignore [missing-import]
from pydantic import BaseModel
# pyrefly: ignore [missing-import]
from google import genai
# pyrefly: ignore [missing-import]
from google.genai import types
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

# 4. Processing endpoint with Batch Embeddings
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
                    config=types.EmbedContentConfig(task_type="RETRIEVAL_DOCUMENT")
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

        # In the new SDK, response.embeddings is the list we need
        embeddings = [e.values for e in response.embeddings]
        print(f"Generated {len(embeddings)} embeddings for {len(chunks)} chunks using {used_model}.")

        # Step 3: Combine text with its embedding
        response_data = []
        
        # SAFETY CHECK: Ensure we have an embedding for every chunk
        if len(embeddings) != len(chunks):
            # If batching failed, we fallback to a simple loop (slower but guaranteed)
            print("Batch size mismatch! Falling back to individual embedding calls...")
            embeddings = []
            for c in chunks:
                res = client.models.embed_content(
                    model=used_model,
                    contents=c,
                    config=types.EmbedContentConfig(task_type="RETRIEVAL_DOCUMENT")
                )
                embeddings.append(res.embeddings[0].values)

        for i in range(len(chunks)):
            response_data.append({
                "content": chunks[i],
                "chunkIndex": i,
                "embedding": embeddings[i]
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

# 5. NEW: Endpoint for Semantic Search Query Embedding
class QueryRequest(BaseModel):
    query: str

@app.post("/embed-query")
async def embed_query(request: QueryRequest):
    print(f"--- Generating Embedding for Query: {request.query[:50]}... ---")
    
    if not client:
        return {"status": "error", "message": "Gemini API key not configured"}

    try:
        # For queries, we use RETRIEVAL_QUERY task type for better matching
        models_to_try = ["models/gemini-embedding-2", "models/gemini-embedding-001"]
        response = None
        used_model = ""

        for model_name in models_to_try:
            try:
                response = client.models.embed_content(
                    model=model_name,
                    contents=request.query,
                    config=types.EmbedContentConfig(task_type="RETRIEVAL_QUERY")
                )
                used_model = model_name
                break 
            except Exception as e:
                if "404" in str(e):
                    continue
                else:
                    raise e

        if not response:
            raise Exception("No supported embedding models found.")

        # Return a single vector (not a list of vectors like in /process)
        return {
            "status": "success",
            "embedding": response.embeddings[0].values,
            "model": used_model
        }

    except Exception as e:
        print(f"Error during query embedding: {str(e)}")
        return {
            "status": "error",
            "message": str(e)
        }

# 6. Endpoint for Final Answer Generation
class GenerateRequest(BaseModel):
    query: str
    context: str

@app.post("/generate-answer")
async def generate_answer(request: GenerateRequest):
    if not client:
        return {"status": "error", "message": "Gemini API key not configured"}

    try:
        # Prompt engineering: Strict rules to avoid hallucination
        prompt = f"""You are a helpful AI assistant.

Use ONLY the provided CONTEXT to answer the QUESTION.

If the answer is not found in the context, respond EXACTLY with:
"I don't know based on the provided documents."

CONTEXT:
{request.context}

QUESTION:
{request.query}
"""
        
        # Use an available text generation model
        response = client.models.generate_content(
            model='models/gemini-2.5-flash',
            contents=prompt,
        )

        return {
            "status": "success",
            "answer": response.text
        }

    except Exception as e:
        return {
            "status": "error",
            "message": str(e)
        }
