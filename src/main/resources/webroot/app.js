'use strict';

/**
 * Fetches a greeting from the server API and displays it in the result div.
 *
 * @param {string} name - The name to greet.
 * @returns {Promise<void>}
 */
async function fetchGreeting(name) {
  const resultDiv = document.getElementById('result');
  resultDiv.classList.add('hidden');

  try {
    const response = await fetch(`/api/greeting?name=${encodeURIComponent(name)}`);
    if (!response.ok) {
      throw new Error(`Server responded with status ${response.status}`);
    }
    const data = await response.json();
    resultDiv.textContent = data.message;
    resultDiv.classList.remove('hidden');
  } catch (error) {
    resultDiv.style.color = '#f87171';
    resultDiv.style.borderColor = '#991b1b';
    resultDiv.textContent = `Error: ${error.message}`;
    resultDiv.classList.remove('hidden');
  }
}

document.getElementById('greetBtn').addEventListener('click', () => {
  const name = document.getElementById('nameInput').value.trim() || 'World';
  fetchGreeting(name);
});

document.getElementById('nameInput').addEventListener('keydown', (event) => {
  if (event.key === 'Enter') {
    const name = event.target.value.trim() || 'World';
    fetchGreeting(name);
  }
});
