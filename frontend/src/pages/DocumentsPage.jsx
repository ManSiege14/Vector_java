import { useState, useEffect } from 'react'
import { getDocuments, insertDocument, deleteDocument } from '../api'

export default function DocumentsPage() {
  const [docs, setDocs]       = useState([])
  const [title, setTitle]     = useState('')
  const [text, setText]       = useState('')
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

      {/* Insert form */}
      <div className="card">
        <h3 style={{ marginBottom: 12, fontSize: 14 }}>Add Document</h3>
        <form onSubmit={handleInsert}>
          <label style={{ fontSize: 12, color: '#666' }}>Title</label>
          <input
            type="text"
            placeholder="e.g. Operating Systems Notes"
            value={title}
            onChange={e => setTitle(e.target.value)}
          />

          <label style={{ fontSize: 12, color: '#666' }}>Text</label>
          <textarea
            placeholder="Paste any text — lecture notes, articles, documentation..."
            style={{ minHeight: 120 }}
            value={text}
            onChange={e => setText(e.target.value)}
          />

          <p className="info" style={{ marginBottom: 8 }}>
            Text is automatically chunked (250 words, 30 overlap) and embedded via Ollama.
          </p>

          <button className="btn" type="submit" disabled={loading}>
            {loading ? 'Embedding...' : 'Embed & Insert'}
          </button>
        </form>

        {error   && <p className="error">{error}</p>}
        {success && <p className="success">{success}</p>}
      </div>

      {/* Document list */}
      <div>
        <h3 style={{ marginBottom: 10, fontSize: 14 }}>
          Stored Documents ({docs.length})
          {' '}
          <button className="btn" style={{ fontSize: 11, padding: '2px 8px' }}
            onClick={loadDocs}>
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