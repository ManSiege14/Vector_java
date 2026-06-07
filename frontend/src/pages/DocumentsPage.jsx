import { useState, useEffect } from 'react'
import { getDocuments, insertDocument, deleteDocument, uploadPdf } from '../api'

export default function DocumentsPage() {
  const [docs, setDocs]       = useState([])
  const [activeTab, setActiveTab] = useState('text') // 'text' or 'pdf'
  const [title, setTitle]     = useState('')
  const [text, setText]       = useState('')
  const [file, setFile]       = useState(null)
  const [loading, setLoading] = useState(false)
  const [fetching, setFetching] = useState(false)
  const [error, setError]     = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    loadDocs()
  }, [])

  async function loadDocs() {
    setFetching(true)
    try {
      const data = await getDocuments()
      setDocs(Array.isArray(data) ? data : (data.documents ?? []))
    } catch {
      setError('Failed to load documents')
    } finally {
      setFetching(false)
    }
  }

  // Handle Text Submission
  async function handleInsert(e) {
    e.preventDefault()
    if (!title.trim() || !text.trim()) {
      setError('Title and text are required')
      return
    }
    setLoading(true)
    setError('')
    setSuccess('')
    try {
      const result = await insertDocument({ title, text })
      const chunkCount = result.chunks ?? result.chunkCount ?? '?'
      setSuccess(`Inserted ${chunkCount} chunk(s) successfully.`)
      setTitle('')
      setText('')
      await loadDocs()
    } catch (e) {
      setError(e.response?.data?.message ?? 'Insert failed. Is Ollama running?')
    } finally {
      setLoading(false)
    }
  }

  // Handle PDF Upload
  async function handleUpload(e) {
    e.preventDefault()
    if (!file) {
      setError('Please select a PDF file')
      return
    }
    setLoading(true)
    setError('')
    setSuccess('')
    try {
      await uploadPdf(file, title || file.name)
      setSuccess('PDF uploaded, processed, and embedded successfully!')
      setFile(null)
      setTitle('')
      await loadDocs()
    } catch (e) {
      setError(e.response?.data?.message ?? 'PDF upload and embedding failed.')
    } finally {
      setLoading(false)
    }
  }

  async function handleDelete(id) {
    try {
      await deleteDocument(id)
      setDocs(prev => prev.filter(d => d.id !== id))
    } catch {
      setError('Delete failed')
    }
  }

  return (
    <div>
      <h1>Documents</h1>

      {/* Unified Input Card */}
      <div className="card" style={{ marginBottom: 24 }}>
        {/* Tab Switchers */}
        <div style={{ display: 'flex', gap: '16px', borderBottom: '1px solid #eee', marginBottom: 16, paddingBottom: 8 }}>
          <button 
            type="button"
            onClick={() => { setActiveTab('text'); setError(''); setSuccess(''); }}
            style={{
              background: 'none', border: 'none', padding: '4px 8px', cursor: 'pointer', fontSize: 14,
              fontWeight: activeTab === 'text' ? 'bold' : 'normal',
              borderBottom: activeTab === 'text' ? '2px solid #333' : 'none',
              color: activeTab === 'text' ? '#000' : '#666'
            }}
          >
            Add Raw Text
          </button>
          <button 
            type="button"
            onClick={() => { setActiveTab('pdf'); setError(''); setSuccess(''); }}
            style={{
              background: 'none', border: 'none', padding: '4px 8px', cursor: 'pointer', fontSize: 14,
              fontWeight: activeTab === 'pdf' ? 'bold' : 'normal',
              borderBottom: activeTab === 'pdf' ? '2px solid #333' : 'none',
              color: activeTab === 'pdf' ? '#000' : '#666'
            }}
          >
            Upload PDF
          </button>
        </div>

        {/* Dynamic Form Content */}
        <form onSubmit={activeTab === 'text' ? handleInsert : handleUpload}>
          <label style={{ fontSize: 12, color: '#666' }}>
            {activeTab === 'text' ? 'Title' : 'Document Title (Optional)'}
          </label>
          <input
            type="text"
            placeholder={activeTab === 'text' ? "e.g. Operating Systems Notes" : "Defaults to file name"}
            value={title}
            onChange={e => setTitle(e.target.value)}
          />

          {activeTab === 'text' ? (
            <>
              <label style={{ fontSize: 12, color: '#666' }}>Text Content</label>
              <textarea
                placeholder="Paste any text — lecture notes, articles, documentation..."
                style={{ minHeight: 120 }}
                value={text}
                onChange={e => setText(e.target.value)}
              />
              <p className="info" style={{ marginBottom: 8 }}>
                Text is automatically chunked (250 words, 30 overlap) and embedded via Ollama.
              </p>
            </>
          ) : (
            <>
              <label style={{ fontSize: 12, color: '#666', display: 'block', marginBottom: 6 }}>Select PDF File</label>
              
              {/* Custom Elegant File Picker */}
              <label 
                style={{
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  justifyContent: 'center',
                  padding: '24px',
                  border: file ? '2px dashed #2e7d32' : '2px dashed #ccc',
                  borderRadius: '6px',
                  backgroundColor: file ? '#f1f8e9' : '#fafafa',
                  cursor: 'pointer',
                  textAlign: 'center',
                  marginBottom: '16px',
                  transition: 'all 0.2s ease-in-out'
                }}
              >
                <span style={{ fontSize: '20px', marginBottom: '4px' }}>{file ? '📄' : '📁'}</span>
                <span style={{ fontSize: '13px', color: file ? '#2e7d32' : '#333', fontWeight: file ? 'bold' : 'normal' }}>
                  {file ? file.name : 'Click to browse or drop your PDF here'}
                </span>
                {file && (
                  <span style={{ fontSize: '11px', color: '#666', marginTop: '4px' }}>
                    ({(file.size / 1024 / 1024).toFixed(2)} MB)
                  </span>
                )}
                {/* Hidden original input */}
                <input
                  type="file"
                  accept=".pdf"
                  onChange={(e) => setFile(e.target.files[0] || null)}
                  style={{ display: 'none' }}
                />
              </label>
            </>
          )}

          <button className="btn" type="submit" disabled={loading || (activeTab === 'pdf' && !file)}>
            {loading 
              ? (activeTab === 'text' ? 'Embedding Text...' : 'Processing PDF...') 
              : (activeTab === 'text' ? 'Embed & Insert' : 'Upload & Embed PDF')
            }
          </button>
        </form>

        {error   && <p className="error" style={{ marginTop: 10 }}>{error}</p>}
        {success && <p className="success" style={{ marginTop: 10 }}>{success}</p>}
      </div>

      {/* Document list */}
      <div>
        <h3 style={{ marginBottom: 10, fontSize: 14 }}>
          Stored Documents ({docs.length})
          {' '}
          <button className="btn" style={{ fontSize: 11, padding: '2px 8px' }} onClick={loadDocs}>
            Refresh
          </button>
        </h3>

        {fetching && <p className="loading">Loading...</p>}

        {!fetching && docs.length === 0 && (
          <p className="info">No documents yet. Add one above.</p>
        )}

        {docs.map(doc => (
          <div className="result-item" key={doc.id}>
            <div className="value" style={{ fontWeight: 'bold' }}>
              {doc.title ?? `Document ${doc.id}`}
            </div>

            {doc.preview && (
              <div style={{ fontSize: 12, color: '#555', margin: '4px 0' }}>
                {doc.preview}
              </div>
            )}

            {doc.text && !doc.preview && (
              <div style={{ fontSize: 12, color: '#555', margin: '4px 0' }}>
                {doc.text.slice(0, 160)}{doc.text.length > 160 ? '…' : ''}
              </div>
            )}

            <div className="meta">
              <span>id: {doc.id}</span>
              {doc.words    && <span>{doc.words} words</span>}
              {doc.chunkIndex != null && <span>chunk {doc.chunkIndex}</span>}
              <button className="btn btn-danger"
                style={{ padding: '2px 8px', fontSize: 11 }}
                onClick={() => handleDelete(doc.id)}>
                Delete
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}