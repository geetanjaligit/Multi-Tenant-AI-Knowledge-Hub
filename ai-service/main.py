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

# 3. Basic Health Check Endpoint
# In Spring Boot terms: @GetMapping("/")
@app.get("/")
async def root():
    return {"status": "AI Service is Up and Running"}

# 4. A sample processing endpoint
# In Spring Boot terms: @PostMapping("/process")
@app.post("/process")
async def process_document(request: ProcessRequest):
    # For now, we just acknowledge receipt
    print(f"Received document {request.document_id} for processing")
    
    return {
        "message": "Data received successfully by Python",
        "document_id": request.document_id,
        "length_processed": len(request.content)
    }
