(() => {
  if (window.teamCameraInstalled) return;
  window.teamCameraInstalled = true;
  function attach() {
    const photos = document.querySelector('#report #photos');
    if (!photos || photos.dataset.teamCamera) return;
    photos.dataset.teamCamera = '1';
    const tools = document.createElement('div');
    const camera = document.createElement('input');
    camera.type = 'file'; camera.accept = 'image/jpeg';
    camera.setAttribute('capture', 'environment'); camera.hidden = true;
    const button = document.createElement('button');
    button.type = 'button'; button.className = 'secondary'; button.textContent = 'Chụp ảnh';
    const info = document.createElement('p'); info.setAttribute('aria-live', 'polite');
    const list = document.createElement('div');
    let previews = [];
    function clearPreviews() { previews.forEach(URL.revokeObjectURL); previews = []; }
    function render() {
      clearPreviews(); list.replaceChildren();
      const files = Array.from(photos.files || []);
      info.textContent = files.length ? `Đã đính kèm ${files.length}/3 ảnh. Bấm Gửi báo cáo để lưu.` : 'Có thể chụp và đính kèm tối đa 3 ảnh.';
      files.forEach((file, index) => {
        const row = document.createElement('div');
        if (file.type.startsWith('image/')) {
          const image = document.createElement('img');
          const url = URL.createObjectURL(file); previews.push(url); image.src = url;
          image.alt = `Ảnh đính kèm ${index + 1}`;
          image.style.cssText = 'width:88px;height:88px;object-fit:cover;border-radius:8px;margin:6px;vertical-align:middle';
          row.append(image);
        }
        const remove = document.createElement('button');
        remove.type = 'button'; remove.className = 'secondary'; remove.textContent = `Bỏ ảnh ${index + 1}`;
        remove.onclick = () => {
          const transfer = new DataTransfer();
          files.forEach((f, i) => { if (i !== index) transfer.items.add(f); });
          photos.files = transfer.files; photos.dispatchEvent(new Event('change', { bubbles: true }));
        };
        row.append(remove); list.append(row);
      });
    }
    button.onclick = () => {
      if (photos.files.length >= 3) { info.textContent = 'Đã đủ 3 ảnh. Bỏ một ảnh trước khi chụp thêm.'; return; }
      camera.click();
    };
    camera.onchange = () => {
      const next = Array.from(camera.files || []); camera.value = '';
      if (!next.length) return;
      const files = [...Array.from(photos.files || []), ...next];
      if (files.length > 3 || files.some(f => f.size > 2 * 1024 * 1024)) {
        info.textContent = 'Tối đa 3 ảnh, mỗi ảnh không quá 2 MB. Ảnh cũ vẫn được giữ.'; return;
      }
      const transfer = new DataTransfer(); files.forEach(f => transfer.items.add(f));
      photos.files = transfer.files; photos.dispatchEvent(new Event('change', { bubbles: true }));
    };
    photos.addEventListener('change', render);
    tools.append(button, camera, info, list);
    // Place outside the file-input label so button clicks do not also open that picker.
    const anchor = photos.closest('label') || photos;
    anchor.after(tools); render();
    const cleanup = new MutationObserver(() => {
      if (!photos.isConnected) { clearPreviews(); cleanup.disconnect(); }
    });
    cleanup.observe(document.body, { childList: true, subtree: true });
  }
  new MutationObserver(attach).observe(document.body, { childList: true, subtree: true });
  attach();
})();
