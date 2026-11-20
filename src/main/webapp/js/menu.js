(() => {
  // Muestra la foto del perfil o deja la inicial si no hay foto.
  document.querySelectorAll('.user-avatar-photo').forEach(photo => {
    photo.addEventListener('load', () => { photo.hidden = false; });
    photo.addEventListener('error', () => { photo.hidden = true; });
    photo.hidden = !photo.complete || photo.naturalWidth === 0;
  });

  // Cierre de menús adaptado de pestanas.js.
  const menus = document.querySelectorAll('.user-menu, .mobile-navigation');

  document.addEventListener('click', event => {
    menus.forEach(menu => {
      if (!menu.contains(event.target)) menu.removeAttribute('open');
    });
  });

  document.addEventListener('keydown', event => {
    if (event.key !== 'Escape') return;
    menus.forEach(menu => {
      if (!menu.open) return;
      menu.removeAttribute('open');
      menu.querySelector('summary').focus();
    });
  });

  const farmPicker = document.querySelector('[data-farm-picker]');
  if (farmPicker) {
    farmPicker.querySelector('select').addEventListener('change', () => farmPicker.requestSubmit());
  }

  document.querySelectorAll('[data-confirm-delete]').forEach(form => {
    form.addEventListener('submit', event => {
      if (event.submitter?.name === 'action' && event.submitter.value !== 'delete') return;
      if (!window.confirm(form.dataset.confirmDelete || '¿Eliminar este animal?')) event.preventDefault();
    });
  });

  // Abre el registro al pulsar en la fila, como en la maqueta.
  document.querySelectorAll('[data-open-row] tbody tr').forEach(row => {
    row.addEventListener('click', event => {
      if (event.target.closest('a, button') || window.getSelection().toString()) return;
      if (event.ctrlKey || event.metaKey || event.shiftKey || event.altKey) return;
      row.querySelector('a[href]')?.click();
    });
  });

  document.querySelectorAll('[data-filter-table]').forEach(field => {
    const table = document.getElementById(field.dataset.filterTable);
    if (!table) return;
    field.addEventListener('input', () => {
      const query = field.value.trim().toLocaleLowerCase('es');
      table.querySelectorAll('tbody tr').forEach(row => {
        row.hidden = !row.textContent.toLocaleLowerCase('es').includes(query);
      });
    });
  });
})();
