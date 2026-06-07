import { useState } from 'react'
import { askRag } from '../api'

export default function AskAIPage() {
  const [question, setQuestion] = useState('')
  const [k, setK]               = useState(3)
  const [loading, setLoading]   = useState(false)
  const [error, setError]       = useState('')
  const [result, setResult]     = useState(null)  // { answer, contexts, model }
  const [history, setHistory]   = useState([])

  async function handleAsk(e) {
    e.preventDefault()
    if (!question.trim()) return
    setLoading(true)
    setError('')
    setResult(null)

    try {
      const data = await askRag({ question, k: parseInt(k) })
      setResult(data)
      setHistory(prev => [{ question, data }, ...prev.slice(0, 9)])
      setQuestion('')
    } catch (e) {
      setError(e.response?.data?.message ?? 'Ask failed. Is Ollama running with documents loaded?')
    } finally {
      setLoading(false)
    }
  }

  function loadFromHistory(entry) {
    setResult(entry.data)
    setQuestion(entry.question)
    setError('')
  }

  return (
    <div>
      <h1>Ask AI</h1>

      <div className="card">
        <form onSubmit={handleAsk}>
          <label style={{ fontSize: 12, color: '#666' }}>
            Question (answers from your uploaded documents)
          </label>
          <textarea
            placeholder="What is dynamic programming?&#10;How does HNSW work?"
            value={question}
            onChange={e => setQuestion(e.target.value)}
            onKeyDown={e => {
              if (e.key === 'Enter' && e.ctrlKey) handleAsk(e)
            }}
          />

          <div className="row">
            <div style={{ width: 120 }}>
              <label style={{ fontSize: 12, color: '#666' }}>Context chunks (k)</label>
              <select value={k} onChange={e => setK(e.target.value)}>
                <option value={1}>1</option>
                <option value={2}>2</option>
                <option value={3}>3</option>
                <option value={5}>5</option>
              </select>
            </div>
            <div style={{ paddingTop: 20 }}>
              <button className="btn" type="submit" disabled={loading}>
                {loading ? 'Thinking...' : 'Ask (Ctrl+Enter)'}
              </button>
            </div>
          </div>
        </form>

        {error && <p className="error">{error}</p>}
      </div>

      {/* Current answer */}
      {result && (
        <div className="card">
          <div style={{ fontSize: 12, color: '#666', marginBottom: 8 }}>
            Model: {result.model ?? 'llm'}
          </div>

          <div className="answer-box">
            {result.answer}
          </div>

          {/* Retrieved contexts */}
          {result.contexts?.length > 0 && (
            <div style={{ marginTop: 16 }}>
              <p style={{ fontSize: 12, color: '#666', marginBottom: 8 }}>
                Retrieved {result.contexts.length} context chunk(s):
              </p>
              {result.contexts.map((ctx, i) => (
                <details key={i} style={{ marginBottom: 6 }}>
                  <summary style={{ fontSize: 12, cursor: 'pointer', color: '#444' }}>
                    [{i + 1}] {ctx.title ?? `Chunk ${ctx.id}`}
                    {ctx.distance != null && (
                      <span style={{ color: '#888', marginLeft: 8 }}>
                        dist: {ctx.distance.toFixed(4)}
                      </span>
                    )}
                  </summary>
                  <div style={{
                    fontSize: 12, color: '#555', padding: '8px 0 0 16px',
                    lineHeight: 1.5
                  }}>
                    {ctx.text ?? ctx.content ?? '(no text)'}
                  </div>
                </details>
              ))}
            </div>
          )}
        </div>
      )}

      {/* History */}
      {history.length > 0 && (
        <div>
          <h3 style={{ fontSize: 14, marginBottom: 10 }}>
            Recent ({history.length})
          </h3>
          {history.map((entry, i) => (
            <div className="result-item" key={i}
              style={{ cursor: 'pointer' }}
              onClick={() => loadFromHistory(entry)}>
              <div className="label">Q</div>
              <div className="value">{entry.question}</div>
              <div className="label" style={{ marginTop: 4 }}>A</div>
              <div style={{ fontSize: 12, color: '#555' }}>
                {entry.data.answer?.slice(0, 120)}
                {entry.data.answer?.length > 120 ? '…' : ''}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}