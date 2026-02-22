// ===================================================
// FileConverter Pro – Main JavaScript
// ===================================================

// ===== NAVBAR =====
function initNavbar() {
  const navbar = document.getElementById('navbar');
  const toggle = document.getElementById('navToggle');
  const menu = document.getElementById('navMenu');

  if (!navbar) return;

  // Scroll effect
  window.addEventListener('scroll', () => {
    navbar.classList.toggle('scrolled', window.scrollY > 20);
  }, { passive: true });

  // Mobile toggle
  if (toggle && menu) {
    toggle.addEventListener('click', (e) => {
      e.stopPropagation();
      const isOpen = menu.classList.toggle('open');
      toggle.classList.toggle('active', isOpen);
      // Close all dropdowns when closing the menu
      if (!isOpen) {
        document.querySelectorAll('.nav-dropdown.open').forEach(d => d.classList.remove('open'));
      }
    });
  }

  // Mobile dropdown toggle
  document.querySelectorAll('.dropdown-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      if (window.innerWidth <= 768) {
        e.preventDefault();
        e.stopPropagation();
        const dropdown = btn.closest('.nav-dropdown');
        const wasOpen = dropdown.classList.contains('open');
        // Close all other dropdowns first
        document.querySelectorAll('.nav-dropdown.open').forEach(d => {
          if (d !== dropdown) d.classList.remove('open');
        });
        dropdown.classList.toggle('open', !wasOpen);
      }
    });
  });

  // Close menu when a dropdown link is clicked (mobile)
  document.querySelectorAll('.dropdown-item').forEach(link => {
    link.addEventListener('click', () => {
      if (window.innerWidth <= 768 && menu) {
        menu.classList.remove('open');
        if (toggle) toggle.classList.remove('active');
        document.querySelectorAll('.nav-dropdown.open').forEach(d => d.classList.remove('open'));
      }
    });
  });

  // Close nav on outside click
  document.addEventListener('click', (e) => {
    if (menu && !navbar.contains(e.target) && window.innerWidth <= 768) {
      menu.classList.remove('open');
      if (toggle) toggle.classList.remove('active');
      document.querySelectorAll('.nav-dropdown.open').forEach(d => d.classList.remove('open'));
    }
  });
}

// Initialize navbar as soon as possible, but ensure DOM is ready
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initNavbar);
} else {
  initNavbar();
}

// ===== PARTICLE SYSTEM =====
function initParticles() {
  const container = document.getElementById('particles');
  if (!container) return;

  const count = window.innerWidth > 768 ? 40 : 15;

  for (let i = 0; i < count; i++) {
    const particle = document.createElement('div');
    const size = Math.random() * 4 + 1;
    const x = Math.random() * 100;
    const delay = Math.random() * 8;
    const duration = Math.random() * 10 + 8;

    particle.style.cssText = `
      position:absolute; left:${x}%; bottom:-10px;
      width:${size}px; height:${size}px;
      background:rgba(108,99,255,${Math.random() * 0.4 + 0.1});
      border-radius:50%;
      animation:particleFloat ${duration}s ${delay}s ease-in-out infinite;
      pointer-events:none;
    `;
    container.appendChild(particle);
  }

  if (!document.getElementById('particleStyle')) {
    const style = document.createElement('style');
    style.id = 'particleStyle';
    style.textContent = `
      @keyframes particleFloat {
        0%{transform:translateY(0) rotate(0deg);opacity:0}
        20%{opacity:0.6}
        80%{opacity:0.4}
        100%{transform:translateY(-100vh) rotate(360deg);opacity:0}
      }
    `;
    document.head.appendChild(style);
  }
}

// ===== TOOL SEARCH =====
function initToolSearch(tools) {
  const input = document.getElementById('toolSearch');
  const results = document.getElementById('searchResults');
  if (!input || !results || !tools || !tools.length) return;

  const toolList = Array.isArray(tools) ? tools : [];

  input.addEventListener('input', function () {
    const q = this.value.toLowerCase().trim();
    if (q.length < 2) { results.classList.remove('visible'); return; }

    const matches = toolList.filter(t =>
      (t.name && t.name.toLowerCase().includes(q)) ||
      (t.description && t.description.toLowerCase().includes(q)) ||
      (t.category && t.category.toLowerCase().includes(q)) ||
      (t.fromFormat && t.fromFormat.toLowerCase().includes(q)) ||
      (t.toFormat && t.toFormat.toLowerCase().includes(q))
    ).slice(0, 8);

    if (!matches.length) {
      results.innerHTML = '<div style="padding:16px;text-align:center;color:#666688;font-size:0.85rem">No tools found</div>';
    } else {
      results.innerHTML = matches.map(t => `
        <a href="/tool/${t.id}" class="search-result-item">
          <span class="s-icon">${t.icon || '🔧'}</span>
          <div>
            <div style="font-weight:600;font-size:0.9rem">${t.name || ''}</div>
            <div style="font-size:0.78rem;color:#9999bb">${t.description || ''}</div>
          </div>
          <span style="margin-left:auto;font-size:0.75rem;padding:2px 8px;border-radius:12px;background:rgba(108,99,255,0.2);color:#6c63ff">${t.category || ''}</span>
        </a>
      `).join('');
    }
    results.classList.add('visible');
  });

  input.addEventListener('focus', function () {
    if (this.value.length >= 2) results.classList.add('visible');
  });

  document.addEventListener('click', (e) => {
    if (!input.contains(e.target) && !results.contains(e.target)) {
      results.classList.remove('visible');
    }
  });

  input.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') results.classList.remove('visible');
  });
}

// ===== CATEGORY TABS =====
function initCategoryTabs() {
  const tabs = document.querySelectorAll('.cat-tab');
  const cards = document.querySelectorAll('.tool-mini-card');
  if (!tabs.length) return;

  tabs.forEach(tab => {
    tab.addEventListener('click', () => {
      tabs.forEach(t => t.classList.remove('active'));
      tab.classList.add('active');

      const cat = tab.getAttribute('data-cat');
      cards.forEach(card => {
        const cardCat = card.getAttribute('data-category');
        if (cat === 'all' || cardCat === cat) {
          card.classList.remove('hidden');
          card.style.animation = 'fadeIn 0.3s ease forwards';
        } else {
          card.classList.add('hidden');
        }
      });
    });
  });
}

// ===== SCROLL ANIMATIONS =====
function initScrollAnimations() {
  const els = document.querySelectorAll('.animate-on-scroll');
  if (!els.length) return;

  const observer = new IntersectionObserver((entries) => {
    entries.forEach((entry, i) => {
      if (entry.isIntersecting) {
        setTimeout(() => entry.target.classList.add('visible'), i * 100);
        observer.unobserve(entry.target);
      }
    });
  }, { threshold: 0.1 });

  els.forEach(el => observer.observe(el));
}

// ===== TOOL PAGE =====
function initToolPage() {
  initDropZone();
  initFormSubmit();
}

function initDropZone() {
  const dropZone = document.getElementById('dropZone');
  const fileInput = document.getElementById('fileInput');
  const preview = document.getElementById('filePreview');
  if (!dropZone || !fileInput) return;

  // Label click handled by <label> element
  dropZone.addEventListener('dragover', (e) => {
    e.preventDefault();
    dropZone.classList.add('dragover');
  });

  dropZone.addEventListener('dragleave', () => {
    dropZone.classList.remove('dragover');
  });

  dropZone.addEventListener('drop', (e) => {
    e.preventDefault();
    dropZone.classList.remove('dragover');
    if (e.dataTransfer.files.length) {
      fileInput.files = e.dataTransfer.files;
      updatePreview(fileInput.files, preview, dropZone);
    }
  });

  fileInput.addEventListener('change', () => {
    updatePreview(fileInput.files, preview, dropZone);
  });
}

function updatePreview(files, preview, dropZone) {
  if (!files || !files.length || !preview) return;
  dropZone.classList.add('has-file');

  preview.innerHTML = Array.from(files).map(f => `
    <div class="preview-item">
      <span class="p-icon">${getFileIcon(f.name)}</span>
      <span class="p-name" title="${f.name}">${f.name}</span>
      <span class="p-size">${formatBytes(f.size)}</span>
    </div>
  `).join('');

  // Hide drop content, show preview
  const content = dropZone.querySelector('.drop-zone-content');
  if (content && files.length > 0) {
    content.style.display = 'none';
  }
}

function getFileIcon(name) {
  const ext = name.split('.').pop().toLowerCase();
  const icons = {
    pdf: '📄', doc: '📝', docx: '📝', xls: '📊', xlsx: '📊',
    ppt: '📑', pptx: '📑', jpg: '🖼️', jpeg: '🖼️', png: '🎨',
    gif: '🎞️', webp: '🌐', bmp: '🖼️', csv: '📋', txt: '📄',
    html: '🌐', json: '{}', mp4: '🎬', mp3: '🎵', wav: '🔊',
    avi: '🎬', mov: '🎬', mkv: '🎬', zip: '📦', rtf: '📄', md: '📖'
  };
  return icons[ext] || '📁';
}

function formatBytes(bytes) {
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
}

function initFormSubmit() {
  const form = document.getElementById('convertForm');
  const overlay = document.getElementById('loadingOverlay');
  if (!form || !overlay) return;

  form.addEventListener('submit', (e) => {
    const fileInput = document.getElementById('fileInput');
    if (!fileInput || !fileInput.files.length) {
      e.preventDefault();
      showToast('error', 'No file selected. Please choose a file to convert.');
      return;
    }

    // Show loading
    overlay.classList.add('active');
    animateLoading();
  });
}

function animateLoading() {
  const progressBar = document.getElementById('progressBar');
  const step1 = document.getElementById('step1');
  const step2 = document.getElementById('step2');
  const step3 = document.getElementById('step3');

  let progress = 0;

  const interval = setInterval(() => {
    if (progress < 30) {
      progress += 2;
      if (step1) { step1.classList.add('active'); }
    } else if (progress < 70) {
      progress += 1.5;
      if (step1) { step1.classList.remove('active'); step1.classList.add('done'); }
      if (step2) { step2.classList.add('active'); }
    } else if (progress < 92) {
      progress += 0.8;
      if (step2) { step2.classList.remove('active'); step2.classList.add('done'); }
      if (step3) { step3.classList.add('active'); }
    } else {
      clearInterval(interval);
    }
    if (progressBar) progressBar.style.width = progress + '%';
  }, 100);
}

// ===== CONFETTI =====
function startConfetti() {
  const canvas = document.getElementById('confettiCanvas');
  if (!canvas) return;

  const ctx = canvas.getContext('2d');
  canvas.width = window.innerWidth;
  canvas.height = window.innerHeight;

  const colors = ['#6c63ff', '#00d2ff', '#f39c12', '#2ecc71', '#e74c3c', '#9b59b6'];
  const particles = [];

  for (let i = 0; i < 150; i++) {
    particles.push({
      x: Math.random() * canvas.width,
      y: Math.random() * canvas.height - canvas.height,
      w: Math.random() * 10 + 5,
      h: Math.random() * 6 + 3,
      color: colors[Math.floor(Math.random() * colors.length)],
      vx: (Math.random() - 0.5) * 3,
      vy: Math.random() * 3 + 2,
      angle: Math.random() * Math.PI * 2,
      spin: (Math.random() - 0.5) * 0.2
    });
  }

  let frame = 0;
  function animate() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    let alive = false;

    particles.forEach(p => {
      p.x += p.vx;
      p.y += p.vy;
      p.vy += 0.05;
      p.angle += p.spin;

      if (p.y < canvas.height + 20) alive = true;

      ctx.save();
      ctx.translate(p.x, p.y);
      ctx.rotate(p.angle);
      ctx.fillStyle = p.color;
      ctx.globalAlpha = Math.max(0, 1 - p.y / canvas.height);
      ctx.fillRect(-p.w / 2, -p.h / 2, p.w, p.h);
      ctx.restore();
    });

    frame++;
    if (alive && frame < 300) requestAnimationFrame(animate);
    else ctx.clearRect(0, 0, canvas.width, canvas.height);
  }

  animate();

  window.addEventListener('resize', () => {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
  });
}

// ===== TOAST =====
function showToast(type, message) {
  const existing = document.querySelector('.toast.dynamic');
  if (existing) existing.remove();

  const toast = document.createElement('div');
  toast.className = 'toast dynamic';
  toast.innerHTML = `
    <div class="toast-icon ${type}">
      <i class="fas fa-${type === 'success' ? 'check' : 'exclamation-triangle'}"></i>
    </div>
    <div class="toast-content">
      <strong>${type === 'success' ? 'Success' : 'Error'}</strong>
      <span>${message}</span>
    </div>
    <button class="toast-close" onclick="this.parentElement.remove()">
      <i class="fas fa-times"></i>
    </button>
  `;
  document.body.appendChild(toast);
  requestAnimationFrame(() => toast.classList.add('show'));
  setTimeout(() => { toast.classList.remove('show'); setTimeout(() => toast.remove(), 300); }, 5000);
}

// ===== THANK YOU PAGE =====
function initThankyouPage() {
  // Star rating
  const stars = document.querySelectorAll('.star');
  const reviewInput = document.getElementById('reviewInput');
  const reviewThanks = document.getElementById('reviewThanks');
  const submitBtn = document.getElementById('submitReview');
  let selectedRating = 0;

  stars.forEach((star, idx) => {
    star.addEventListener('mouseenter', () => {
      stars.forEach((s, i) => s.style.filter = i <= idx ? 'grayscale(0)' : 'grayscale(1)');
    });
    star.addEventListener('mouseleave', () => {
      stars.forEach((s, i) => s.style.filter = i < selectedRating ? 'grayscale(0)' : 'grayscale(1)');
    });
    star.addEventListener('click', () => {
      selectedRating = idx + 1;
      stars.forEach((s, i) => {
        s.style.filter = i < selectedRating ? 'grayscale(0)' : 'grayscale(1)';
        s.classList.toggle('active', i < selectedRating);
      });
      if (reviewInput) reviewInput.style.display = 'block';
    });
  });

  if (submitBtn) {
    submitBtn.addEventListener('click', () => {
      if (reviewInput) reviewInput.style.display = 'none';
      if (reviewThanks) reviewThanks.style.display = 'flex';
    });
  }

  // Countdown redirect
  const totalSec = 10;
  const circle = document.getElementById('counterCircle');
  const numEl = document.getElementById('counterNum');
  const cdEl = document.getElementById('countdownSec');
  const circumference = 213.6; // 2 * π * 34

  let remaining = totalSec;

  const timer = setInterval(() => {
    remaining--;
    if (numEl) numEl.textContent = remaining;
    if (cdEl) cdEl.textContent = remaining;
    if (circle) {
      const offset = circumference * (1 - remaining / totalSec);
      circle.style.strokeDashoffset = offset;
    }
    if (remaining <= 0) {
      clearInterval(timer);
      window.location.href = '/';
    }
  }, 1000);
}

// ===== STATS PAGE =====
function initStatsPage(statsData, totalConversions, successCount, failureCount) {
  const pieCtx = document.getElementById('pieChart');
  const barCtx = document.getElementById('barChart');

  const labels = Object.keys(statsData);
  const values = Object.values(statsData);

  const COLORS = ['#6c63ff', '#00d2ff', '#f39c12', '#2ecc71', '#e74c3c', '#9b59b6', '#1abc9c', '#e67e22'];

  if (pieCtx && labels.length > 0) {
    new Chart(pieCtx, {
      type: 'doughnut',
      data: {
        labels,
        datasets: [{
          data: values,
          backgroundColor: COLORS.slice(0, labels.length).map(c => c + 'cc'),
          borderColor: COLORS.slice(0, labels.length),
          borderWidth: 2,
          hoverOffset: 12
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: true,
        cutout: '65%',
        plugins: {
          legend: { display: false },
          tooltip: {
            backgroundColor: '#1a1a2e',
            borderColor: '#2a2a4a',
            borderWidth: 1,
            titleColor: '#e8e8f0',
            bodyColor: '#9999bb',
            callbacks: {
              label: (ctx) => ` ${ctx.label}: ${ctx.raw} (${totalConversions > 0 ? Math.round(ctx.raw * 100 / totalConversions) : 0}%)`
            }
          }
        },
        animation: {
          animateScale: true,
          animateRotate: true,
          duration: 1200,
          easing: 'easeInOutQuart'
        }
      }
    });

    // Build legend
    const legend = document.getElementById('chartLegend');
    if (legend) {
      legend.innerHTML = labels.map((l, i) => `
        <div class="legend-item">
          <div class="legend-dot" style="background:${COLORS[i % COLORS.length]}"></div>
          <span>${l}: <strong>${values[i]}</strong></span>
        </div>
      `).join('');
    }
  } else if (pieCtx) {
    const ctx2 = pieCtx.getContext('2d');
    ctx2.fillStyle = '#9999bb';
    ctx2.font = '14px Inter';
    ctx2.textAlign = 'center';
    ctx2.fillText('No data yet – start converting files!', pieCtx.width / 2, pieCtx.height / 2);
  }

  if (barCtx) {
    new Chart(barCtx, {
      type: 'bar',
      data: {
        labels: ['Conversions'],
        datasets: [
          {
            label: 'Successful',
            data: [successCount],
            backgroundColor: 'rgba(46,204,113,0.7)',
            borderColor: '#2ecc71',
            borderWidth: 2,
            borderRadius: 8,
            barThickness: 60
          },
          {
            label: 'Failed',
            data: [failureCount],
            backgroundColor: 'rgba(231,76,60,0.7)',
            borderColor: '#e74c3c',
            borderWidth: 2,
            borderRadius: 8,
            barThickness: 60
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: true,
        scales: {
          x: { grid: { color: '#2a2a4a' }, ticks: { color: '#9999bb' } },
          y: { grid: { color: '#2a2a4a' }, ticks: { color: '#9999bb' }, beginAtZero: true }
        },
        plugins: {
          legend: {
            labels: { color: '#e8e8f0', usePointStyle: true, boxWidth: 10 }
          },
          tooltip: {
            backgroundColor: '#1a1a2e',
            borderColor: '#2a2a4a',
            borderWidth: 1,
            titleColor: '#e8e8f0',
            bodyColor: '#9999bb'
          }
        },
        animation: { duration: 1000, easing: 'easeInOutQuart' }
      }
    });
  }
}

// ===== ERROR PAGE =====
function initErrorPage() {
  // Animate robot eyes to follow mouse
  const eye1 = document.getElementById('eye1');
  const eye2 = document.getElementById('eye2');
  if (!eye1 || !eye2) return;

  document.addEventListener('mousemove', (e) => {
    [eye1, eye2].forEach(eye => {
      const rect = eye.getBoundingClientRect();
      const cx = rect.left + rect.width / 2;
      const cy = rect.top + rect.height / 2;
      const angle = Math.atan2(e.clientY - cy, e.clientX - cx);
      const dist = 4;
      const pupil = eye.querySelector('.pupil');
      if (pupil) {
        pupil.style.transform = `translate(${Math.cos(angle) * dist}px, ${Math.sin(angle) * dist}px)`;
      }
    });
  });
}

// ===== NUMBER COUNTER ANIMATION =====
function animateCounters() {
  document.querySelectorAll('.stat-number, .stats-card-number').forEach(el => {
    const target = parseInt(el.textContent.replace(/[^0-9]/g, ''), 10);
    if (!target || target === 0) return;

    let current = 0;
    const step = target / 60;
    const timer = setInterval(() => {
      current = Math.min(current + step, target);
      el.textContent = Math.round(current);
      if (current >= target) clearInterval(timer);
    }, 16);
  });
}

// ===== DOM READY =====
document.addEventListener('DOMContentLoaded', () => {
  initScrollAnimations();

  // Animate counters on page load
  const hasCounters = document.querySelector('.stat-number, .stats-card-number');
  if (hasCounters) setTimeout(animateCounters, 500);

  // Scroll to top button
  const scrollBtn = document.createElement('button');
  scrollBtn.innerHTML = '<i class="fas fa-chevron-up"></i>';
  scrollBtn.style.cssText = `
    position:fixed; bottom:32px; right:32px; z-index:1000;
    width:44px; height:44px; border-radius:50%;
    background:linear-gradient(135deg,#6c63ff,#00d2ff);
    border:none; color:white; cursor:pointer;
    display:none; align-items:center; justify-content:center;
    box-shadow:0 4px 20px rgba(108,99,255,0.4);
    transition:all 0.3s ease; font-size:0.9rem;
  `;
  document.body.appendChild(scrollBtn);

  window.addEventListener('scroll', () => {
    scrollBtn.style.display = window.scrollY > 400 ? 'flex' : 'none';
  }, { passive: true });

  scrollBtn.addEventListener('click', () => window.scrollTo({ top: 0, behavior: 'smooth' }));
});
