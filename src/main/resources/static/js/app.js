/**
 * ExpenseTracker Classic & Modern Dashboard JavaScript Client
 */

// Determine API Base URL dynamically
const API_BASE = window.location.protocol === 'file:' ? 'http://localhost:8080' : '';

const CATEGORIES = {
  income: ['Salary', 'Investment', 'Freelancing', 'Business'],
  expenses: ['Food', 'Travel', 'Shopping', 'Bills', 'Health', 'Entertainment']
};

const CATEGORY_ICONS = {
  Salary: '💰',
  Investment: '📈',
  Freelancing: '💻',
  Business: '🏢',
  Food: '🍔',
  Travel: '✈️',
  Shopping: '🛍️',
  Bills: '📄',
  Health: '🏥',
  Entertainment: '🎬'
};

// Global App State
const state = {
  selectedFormType: 'expenses', // 'expenses' or 'income'
  activeFilter: 'all',
  currentPage: 0,
  pageSize: 10,
  totalPages: 1,
  totalElements: 0,
  sortBy: 'date,desc',
  searchQuery: '',
  selectedCategoryFilter: '',
  selectedDate: '',
  startDate: '',
  endDate: '',
  minAmount: null,
  maxAmount: null
};

// ==========================================================================
// Initialization
// ==========================================================================
document.addEventListener('DOMContentLoaded', () => {
  initForm();
  initFilters();
  initSearch();
  initReportModal();
  refreshDashboard();
  checkApiHealth();
});

function refreshDashboard() {
  loadBalance();
  loadHighlights();
  loadCategorySummary();
  loadTransactions();
}

// ==========================================================================
// API Health Indicator
// ==========================================================================
async function checkApiHealth() {
  const badge = document.getElementById('apiStatusBadge');
  try {
    const res = await fetch(`${API_BASE}/api/transactions/balance`);
    if (res.ok) {
      badge.innerHTML = `<span class="status-dot"></span> API Connected`;
      badge.style.borderColor = 'rgba(16, 185, 129, 0.4)';
      badge.style.color = '#10b981';
    } else {
      throw new Error();
    }
  } catch (e) {
    badge.innerHTML = `<span class="status-dot" style="background: #f43f5e; box-shadow: 0 0 8px #f43f5e;"></span> Offline / Disconnected`;
    badge.style.borderColor = 'rgba(244, 63, 94, 0.4)';
    badge.style.color = '#f43f5e';
  }
}

// ==========================================================================
// KPI & Balance Loading
// ==========================================================================
async function loadBalance() {
  try {
    const res = await fetch(`${API_BASE}/api/transactions/balance`);
    if (!res.ok) throw new Error('Failed to fetch balance');
    const data = await res.json();

    const netBalEl = document.getElementById('kpiNetBalance');
    const incomeEl = document.getElementById('kpiTotalIncome');
    const expenseEl = document.getElementById('kpiTotalExpense');

    netBalEl.textContent = formatCurrency(data.balance);
    incomeEl.textContent = formatCurrency(data.totalIncome);
    expenseEl.textContent = formatCurrency(data.totalExpense);

    if (data.balance >= 0) {
      netBalEl.style.color = '#e0f2fe';
    } else {
      netBalEl.style.color = '#f87171';
    }
  } catch (err) {
    console.error('Error loading balance:', err);
  }
}

async function loadHighlights() {
  try {
    const [incRes, expRes] = await Promise.all([
      fetch(`${API_BASE}/api/transactions/highest-income`),
      fetch(`${API_BASE}/api/transactions/highest-expense`)
    ]);

    const highestIncome = incRes.ok ? await incRes.json() : null;
    const highestExpense = expRes.ok ? await expRes.json() : null;

    const topIncEl = document.getElementById('highlightTopIncome');
    const topExpEl = document.getElementById('highlightTopExpense');

    topIncEl.textContent = highestIncome && highestIncome.amount 
      ? `${formatCurrency(highestIncome.amount)} (${highestIncome.catagory || 'Income'})` 
      : 'None';

    topExpEl.textContent = highestExpense && highestExpense.amount 
      ? `${formatCurrency(highestExpense.amount)} (${highestExpense.catagory || 'Expense'})` 
      : 'None';
  } catch (err) {
    console.error('Error loading highlights:', err);
  }
}

// ==========================================================================
// Category Breakdown Summary
// ==========================================================================
async function loadCategorySummary() {
  const container = document.getElementById('categorySummaryList');
  try {
    const res = await fetch(`${API_BASE}/api/transactions/category-summary`);
    if (!res.ok) throw new Error('Failed to load category summary');
    const summary = await res.json();

    const entries = Object.entries(summary);
    if (entries.length === 0) {
      container.innerHTML = `<div style="color: var(--text-dim); font-size: 0.85rem; text-align: center; padding: 1.5rem 0;">No expense records yet</div>`;
      return;
    }

    const totalSpent = entries.reduce((acc, [, val]) => acc + val, 0);

    container.innerHTML = entries.map(([category, amount]) => {
      const pct = totalSpent > 0 ? Math.round((amount / totalSpent) * 100) : 0;
      const icon = CATEGORY_ICONS[category] || '🏷️';
      return `
        <div class="category-item">
          <div class="cat-header">
            <span class="cat-name">${icon} ${category}</span>
            <span class="cat-amount">${formatCurrency(amount)} <span style="color: var(--text-dim); font-weight: normal; font-size: 0.75rem;">(${pct}%)</span></span>
          </div>
          <div class="progress-track">
            <div class="progress-fill" style="width: ${pct}%;"></div>
          </div>
        </div>
      `;
    }).join('');
  } catch (err) {
    console.error('Error loading category summary:', err);
  }
}

// ==========================================================================
// Form & Transaction Input
// ==========================================================================
function initForm() {
  const expenseToggle = document.getElementById('toggleExpense');
  const incomeToggle = document.getElementById('toggleIncome');
  const categorySelect = document.getElementById('transCategory');
  const dateInput = document.getElementById('transDate');
  const form = document.getElementById('transactionForm');

  // Default to today's date in local ISO format (YYYY-MM-DD)
  const today = new Date().toISOString().split('T')[0];
  dateInput.value = today;

  // Toggle Type buttons
  expenseToggle.addEventListener('click', () => {
    state.selectedFormType = 'expenses';
    expenseToggle.classList.add('active');
    incomeToggle.classList.remove('active');
    populateCategories('expenses');
  });

  incomeToggle.addEventListener('click', () => {
    state.selectedFormType = 'income';
    incomeToggle.classList.add('active');
    expenseToggle.classList.remove('active');
    populateCategories('income');
  });

  // Populate initial categories
  populateCategories('expenses');

  // Form Submit Handler
  form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const amountVal = parseFloat(document.getElementById('transAmount').value);
    const categoryVal = categorySelect.value;
    const dateVal = dateInput.value;
    const descriptionVal = document.getElementById('transDescription').value.trim();

    if (isNaN(amountVal) || amountVal <= 0) {
      showToast('Please enter a valid amount greater than 0', 'error');
      return;
    }

    if (!categoryVal) {
      showToast('Please select a category', 'error');
      return;
    }

    if (!dateVal) {
      showToast('Please pick a date', 'error');
      return;
    }

    const payload = {
      amount: amountVal,
      date: new Date(dateVal + 'T12:00:00.000Z').toISOString(),
      description: descriptionVal || `${categoryVal} transaction`,
      catagory: categoryVal,
      transType: state.selectedFormType
    };

    const submitBtn = form.querySelector('button[type="submit"]');
    const originalBtnText = submitBtn.innerHTML;
    submitBtn.disabled = true;
    submitBtn.innerHTML = `<span>Saving...</span>`;

    try {
      const res = await fetch(`${API_BASE}/api/transactions/create`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (!res.ok) {
        throw new Error('Failed to create transaction');
      }

      showToast(`Added ${state.selectedFormType === 'income' ? 'Income' : 'Expense'} of ${formatCurrency(amountVal)}!`, 'success');
      form.reset();
      dateInput.value = today;
      populateCategories(state.selectedFormType);

      // Refresh all views immediately
      refreshDashboard();
    } catch (err) {
      showToast(err.message || 'Error saving transaction', 'error');
    } finally {
      submitBtn.disabled = false;
      submitBtn.innerHTML = originalBtnText;
    }
  });
}

function populateCategories(type) {
  const categorySelect = document.getElementById('transCategory');
  const cats = CATEGORIES[type] || [];
  categorySelect.innerHTML = cats.map(c => `<option value="${c}">${CATEGORY_ICONS[c] || '🏷️'} ${c}</option>`).join('');
}

// ==========================================================================
// Filter & Search Controls
// ==========================================================================
function initFilters() {
  const pills = document.querySelectorAll('.filter-pills .pill-btn[data-filter]');
  pills.forEach(pill => {
    pill.addEventListener('click', () => {
      pills.forEach(p => p.classList.remove('active'));
      pill.classList.add('active');

      state.activeFilter = pill.dataset.filter;
      state.currentPage = 0;
      loadTransactions();
    });
  });

  const catFilterSelect = document.getElementById('filterCategorySelect');
  const allCategories = [...CATEGORIES.income, ...CATEGORIES.expenses];
  catFilterSelect.innerHTML = `<option value="">Category: All</option>` +
    allCategories.map(c => `<option value="${c}">${c}</option>`).join('');

  catFilterSelect.addEventListener('change', () => {
    state.selectedCategoryFilter = catFilterSelect.value;
    if (state.selectedCategoryFilter) {
      state.activeFilter = 'category';
      pills.forEach(p => p.classList.remove('active'));
    } else {
      state.activeFilter = 'all';
      document.querySelector('.pill-btn[data-filter="all"]').classList.add('active');
    }
    state.currentPage = 0;
    loadTransactions();
  });

  // Extra filter fields
  const dateFilterInput = document.getElementById('filterDate');
  dateFilterInput.addEventListener('change', () => {
    state.selectedDate = dateFilterInput.value;
    if (state.selectedDate) {
      state.activeFilter = 'date';
      pills.forEach(p => p.classList.remove('active'));
    }
    state.currentPage = 0;
    loadTransactions();
  });

  const startDateInput = document.getElementById('filterStartDate');
  const endDateInput = document.getElementById('filterEndDate');
  const applyDateRangeBtn = document.getElementById('btnApplyDateRange');
  if (applyDateRangeBtn) {
    applyDateRangeBtn.addEventListener('click', () => {
      state.startDate = startDateInput.value;
      state.endDate = endDateInput.value;
      if (state.startDate || state.endDate) {
        state.activeFilter = 'date-range';
        pills.forEach(p => p.classList.remove('active'));
        state.currentPage = 0;
        loadTransactions();
      }
    });
  }

  const minAmtInput = document.getElementById('filterMinAmount');
  const maxAmtInput = document.getElementById('filterMaxAmount');
  const applyAmtRangeBtn = document.getElementById('btnApplyAmountRange');
  if (applyAmtRangeBtn) {
    applyAmtRangeBtn.addEventListener('click', () => {
      state.minAmount = minAmtInput.value ? parseFloat(minAmtInput.value) : null;
      state.maxAmount = maxAmtInput.value ? parseFloat(maxAmtInput.value) : null;
      if (state.minAmount !== null || state.maxAmount !== null) {
        state.activeFilter = 'amount-range';
        pills.forEach(p => p.classList.remove('active'));
        state.currentPage = 0;
        loadTransactions();
      }
    });
  }

  // Reset Filters button
  document.getElementById('btnResetFilters').addEventListener('click', () => {
    state.activeFilter = 'all';
    state.searchQuery = '';
    state.selectedCategoryFilter = '';
    state.selectedDate = '';
    state.startDate = '';
    state.endDate = '';
    state.minAmount = null;
    state.maxAmount = null;
    state.currentPage = 0;

    document.getElementById('searchInput').value = '';
    catFilterSelect.value = '';
    dateFilterInput.value = '';
    if (startDateInput) startDateInput.value = '';
    if (endDateInput) endDateInput.value = '';
    if (minAmtInput) minAmtInput.value = '';
    if (maxAmtInput) maxAmtInput.value = '';

    pills.forEach(p => p.classList.remove('active'));
    document.querySelector('.pill-btn[data-filter="all"]').classList.add('active');

    loadTransactions();
  });

  // Pagination button handlers
  document.getElementById('btnPrevPage').addEventListener('click', () => {
    if (state.currentPage > 0) {
      state.currentPage--;
      loadTransactions();
    }
  });

  document.getElementById('btnNextPage').addEventListener('click', () => {
    if (state.currentPage < state.totalPages - 1) {
      state.currentPage++;
      loadTransactions();
    }
  });
}

function initSearch() {
  const searchInput = document.getElementById('searchInput');
  let debounceTimeout = null;

  searchInput.addEventListener('input', () => {
    clearTimeout(debounceTimeout);
    debounceTimeout = setTimeout(() => {
      const q = searchInput.value.trim();
      state.searchQuery = q;
      if (q) {
        state.activeFilter = 'search';
        document.querySelectorAll('.filter-pills .pill-btn').forEach(p => p.classList.remove('active'));
      } else {
        state.activeFilter = 'all';
        document.querySelector('.pill-btn[data-filter="all"]').classList.add('active');
      }
      state.currentPage = 0;
      loadTransactions();
    }, 300);
  });
}

// ==========================================================================
// Transaction Loading & Rendering
// ==========================================================================
async function loadTransactions() {
  const tbody = document.getElementById('transactionsTableBody');
  const countBadge = document.getElementById('tableCountBadge');
  const paginationInfo = document.getElementById('paginationInfo');
  const btnPrev = document.getElementById('btnPrevPage');
  const btnNext = document.getElementById('btnNextPage');

  tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; padding: 2rem; color: var(--text-dim);">Loading transactions...</td></tr>`;

  try {
    let url = '';
    let isPaginated = false;

    if (state.activeFilter === 'all') {
      url = `${API_BASE}/api/transactions?page=${state.currentPage}&size=${state.pageSize}&sort=${state.sortBy}`;
      isPaginated = true;
    } else if (state.activeFilter === 'income') {
      url = `${API_BASE}/api/transactions/income`;
    } else if (state.activeFilter === 'expenses') {
      url = `${API_BASE}/api/transactions/expenses`;
    } else if (state.activeFilter === 'category') {
      url = `${API_BASE}/api/transactions/category/${encodeURIComponent(state.selectedCategoryFilter)}`;
    } else if (state.activeFilter === 'search') {
      url = `${API_BASE}/api/transactions/search?keyword=${encodeURIComponent(state.searchQuery)}`;
    } else if (state.activeFilter === 'date') {
      url = `${API_BASE}/api/transactions/date/${encodeURIComponent(state.selectedDate)}`;
    } else if (state.activeFilter === 'date-range') {
      url = `${API_BASE}/api/transactions/date-range?startDate=${encodeURIComponent(state.startDate)}&endDate=${encodeURIComponent(state.endDate)}`;
    } else if (state.activeFilter === 'amount-range') {
      const min = state.minAmount !== null ? state.minAmount : 0;
      const max = state.maxAmount !== null ? state.maxAmount : 1000000;
      url = `${API_BASE}/api/transactions/amount-range?min=${min}&max=${max}`;
    }

    const res = await fetch(url);
    if (!res.ok) throw new Error('Failed to retrieve transactions');
    const data = await res.json();

    let list = [];
    if (isPaginated && data.content) {
      list = data.content;
      state.totalPages = data.totalPages || 1;
      state.totalElements = data.totalElements || 0;
      paginationInfo.textContent = `Page ${state.currentPage + 1} of ${Math.max(state.totalPages, 1)} (${state.totalElements} records)`;
      btnPrev.disabled = state.currentPage <= 0;
      btnNext.disabled = state.currentPage >= state.totalPages - 1;
    } else {
      list = Array.isArray(data) ? data : [];
      state.totalPages = 1;
      state.totalElements = list.length;
      paginationInfo.textContent = `Showing ${list.length} records`;
      btnPrev.disabled = true;
      btnNext.disabled = true;
    }

    countBadge.textContent = `${state.totalElements} items`;
    renderTable(list);
  } catch (err) {
    console.error('Error loading transactions:', err);
    tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; padding: 2rem; color: var(--expense-color);">Error loading records. Make sure the API server is running.</td></tr>`;
  }
}

function renderTable(list) {
  const tbody = document.getElementById('transactionsTableBody');

  if (!list || list.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="5">
          <div class="empty-state">
            <div class="empty-state-icon">💳</div>
            <h3>No Transactions Found</h3>
            <p>Add a new transaction using the form or adjust your search filter.</p>
          </div>
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = list.map(tx => {
    const isIncome = tx.transType === 'income';
    const amountSign = isIncome ? '+' : '-';
    const amountClass = isIncome ? 'income' : 'expense';
    const icon = CATEGORY_ICONS[tx.catagory] || '🏷️';
    const dateFormatted = tx.date ? new Date(tx.date).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    }) : 'N/A';

    return `
      <tr>
        <td>
          <div class="tx-desc" title="${escapeHtml(tx.description || '')}">${escapeHtml(tx.description || 'Untitled')}</div>
        </td>
        <td>
          <span class="category-tag">${icon} ${escapeHtml(tx.catagory || 'Other')}</span>
        </td>
        <td class="tx-date">${dateFormatted}</td>
        <td>
          <span class="tx-amount ${amountClass}">${amountSign} ${formatCurrency(tx.amount)}</span>
        </td>
        <td style="text-align: right;">
          <button class="btn-delete" title="Delete Transaction" onclick="handleDelete(${tx.id})">
            <svg width="18" height="18" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
            </svg>
          </button>
        </td>
      </tr>
    `;
  }).join('');
}

// Global delete handler
window.handleDelete = async function(id) {
  if (!confirm('Are you sure you want to delete this transaction?')) {
    return;
  }

  try {
    const res = await fetch(`${API_BASE}/api/transactions/${id}`, {
      method: 'DELETE'
    });

    if (!res.ok) throw new Error('Failed to delete transaction');

    showToast('Transaction deleted successfully', 'success');
    refreshDashboard();
  } catch (err) {
    showToast(err.message || 'Error deleting transaction', 'error');
  }
};

// ==========================================================================
// Reports Modal (Monthly & Yearly)
// ==========================================================================
function initReportModal() {
  const modal = document.getElementById('reportModal');
  const btnOpen = document.getElementById('btnOpenReportModal');
  const btnClose = document.getElementById('btnCloseReportModal');
  const tabMonthly = document.getElementById('tabReportMonthly');
  const tabYearly = document.getElementById('tabReportYearly');

  btnOpen.addEventListener('click', () => {
    modal.classList.add('active');
    loadMonthlyReportView();
  });

  btnClose.addEventListener('click', () => {
    modal.classList.remove('active');
  });

  modal.addEventListener('click', (e) => {
    if (e.target === modal) {
      modal.classList.remove('active');
    }
  });

  tabMonthly.addEventListener('click', () => {
    tabMonthly.classList.add('active');
    tabYearly.classList.remove('active');
    loadMonthlyReportView();
  });

  tabYearly.addEventListener('click', () => {
    tabYearly.classList.add('active');
    tabMonthly.classList.remove('active');
    loadYearlyReportView();
  });
}

async function loadMonthlyReportView() {
  const content = document.getElementById('reportModalContent');
  content.innerHTML = `<div style="text-align: center; padding: 2rem; color: var(--text-dim);">Loading monthly reports...</div>`;
  try {
    const res = await fetch(`${API_BASE}/api/transactions/monthly`);
    if (!res.ok) throw new Error('Failed to load monthly reports');
    const reports = await res.json();

    if (!reports || reports.length === 0) {
      content.innerHTML = `<div style="text-align: center; padding: 2rem; color: var(--text-dim);">No monthly reports available.</div>`;
      return;
    }

    content.innerHTML = `
      <table class="custom-table" style="margin-top: 1rem;">
        <thead>
          <tr>
            <th>Month</th>
            <th>Total Income</th>
            <th>Total Expense</th>
            <th>Net Balance</th>
            <th>Tx Count</th>
          </tr>
        </thead>
        <tbody>
          ${reports.map(r => `
            <tr>
              <td><strong>${r.month}</strong></td>
              <td style="color: var(--income-color); font-weight: 600;">+ ${formatCurrency(r.totalIncome)}</td>
              <td style="color: var(--expense-color); font-weight: 600;">- ${formatCurrency(r.totalExpense)}</td>
              <td style="font-weight: 700; color: ${r.balance >= 0 ? '#38bdf8' : '#f87171'};">${formatCurrency(r.balance)}</td>
              <td><span class="count-badge">${r.transactionCount}</span></td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    `;
  } catch (err) {
    content.innerHTML = `<div style="color: var(--expense-color); padding: 1.5rem;">Error loading monthly report: ${err.message}</div>`;
  }
}

async function loadYearlyReportView() {
  const content = document.getElementById('reportModalContent');
  content.innerHTML = `<div style="text-align: center; padding: 2rem; color: var(--text-dim);">Loading yearly reports...</div>`;
  try {
    const res = await fetch(`${API_BASE}/api/transactions/yearly`);
    if (!res.ok) throw new Error('Failed to load yearly reports');
    const reports = await res.json();

    if (!reports || reports.length === 0) {
      content.innerHTML = `<div style="text-align: center; padding: 2rem; color: var(--text-dim);">No yearly reports available.</div>`;
      return;
    }

    content.innerHTML = `
      <table class="custom-table" style="margin-top: 1rem;">
        <thead>
          <tr>
            <th>Year</th>
            <th>Total Income</th>
            <th>Total Expense</th>
            <th>Net Balance</th>
            <th>Tx Count</th>
          </tr>
        </thead>
        <tbody>
          ${reports.map(r => `
            <tr>
              <td><strong>${r.year}</strong></td>
              <td style="color: var(--income-color); font-weight: 600;">+ ${formatCurrency(r.totalIncome)}</td>
              <td style="color: var(--expense-color); font-weight: 600;">- ${formatCurrency(r.totalExpense)}</td>
              <td style="font-weight: 700; color: ${r.balance >= 0 ? '#38bdf8' : '#f87171'};">${formatCurrency(r.balance)}</td>
              <td><span class="count-badge">${r.transactionCount}</span></td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    `;
  } catch (err) {
    content.innerHTML = `<div style="color: var(--expense-color); padding: 1.5rem;">Error loading yearly report: ${err.message}</div>`;
  }
}

// ==========================================================================
// Utilities & Toasts
// ==========================================================================
function formatCurrency(num) {
  const n = typeof num === 'number' ? num : 0;
  return '₹' + n.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function escapeHtml(str) {
  return str.replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
}

function showToast(message, type = 'success') {
  const container = document.getElementById('toastContainer');
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  const icon = type === 'success' ? '✓' : '⚠️';
  toast.innerHTML = `<span style="font-weight: bold;">${icon}</span> <span>${escapeHtml(message)}</span>`;

  container.appendChild(toast);

  setTimeout(() => {
    toast.style.transition = 'opacity 0.4s ease, transform 0.4s ease';
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(10px)';
    setTimeout(() => toast.remove(), 400);
  }, 3500);
}
