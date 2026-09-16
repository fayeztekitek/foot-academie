import api from './client';

export const aiApi = {
  chat: (message, page) =>
    api.post('/ai/chat', { message, page }).then(r => r.data),

  chatStream: async function* (message, page) {
    const token = localStorage.getItem('accessToken');
    const response = await fetch(`${api.defaults.baseURL}/ai/chat/stream`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
      },
      body: JSON.stringify({ message, page }),
    });

    const reader = response.body.getReader();
    const decoder = new TextDecoder();

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      const text = decoder.decode(value);
      const lines = text.split('\n');
      for (const line of lines) {
        if (line.startsWith('data: ')) {
          const data = line.slice(6);
          if (data === 'true') return;
          yield data;
        }
      }
    }
  },

  analyzeDocument: (file, page) => {
    const formData = new FormData();
    formData.append('file', file);
    if (page) formData.append('page', page);
    return api.post('/ai/analyze-document', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }).then(r => r.data);
  },

  getSuggestions: (page) =>
    api.get('/ai/suggestions', { params: { page } }).then(r => r.data),

  clearHistory: () =>
    api.post('/ai/clear').then(r => r.data),

  getConfig: () =>
    api.get('/ai/config').then(r => r.data),
};
