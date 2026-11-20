(() => {
  // Como en pestanas.js, se puede abrir la ficha desde la fila.
  document.querySelectorAll('.event-results-table tbody tr').forEach(row => {
    row.addEventListener('click', event => {
      if (event.target.closest('a, button') || window.getSelection().toString()) return;
      if (event.ctrlKey || event.metaKey || event.shiftKey || event.altKey) return;
      row.querySelector('a[href]').click();
    });
  });

  const type = document.querySelector('#event-type');
  const treatment = document.querySelector('#event-treatment');
  const dose = document.querySelector('#event-dose');
  if (!type || !treatment || !dose) return;

  function filterDoses() {
    // Solo se ofrecen las dosis del tratamiento elegido.
    for (const option of dose.options) {
      option.disabled = Boolean(option.value) && option.dataset.tratamiento !== treatment.value;
      option.hidden = option.disabled;
    }
    if (dose.selectedOptions[0]?.disabled) dose.value = '';
  }

  treatment.addEventListener('change', filterDoses);
  filterDoses();
})();
