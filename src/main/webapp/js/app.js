/* Shared JavaScript — connects HTML pages to the Java backend API */

async function apiGet(url) {
    return requestApi(url, { credentials: 'include' });
}

async function apiPost(url, body) {
    return requestApi(url, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });
}

async function apiPut(url, body) {
    return requestApi(url, {
        method: 'PUT',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });
}

async function apiDelete(url) {
    return requestApi(url, {
        method: 'DELETE',
        credentials: 'include'
    });
}

async function requestApi(url, options) {
    try {
        const response = await fetch(API_BASE + url, options);
        const text = await response.text();

        try {
            return JSON.parse(text);
        } catch (error) {
            return {
                success: false,
                message: `Java API error (${response.status}). Restart the application and try again.`
            };
        }
    } catch (error) {
        return {
            success: false,
            message: 'Cannot connect to the Java server. Start DentalClinicApplication and try again.'
        };
    }
}

function showAlert(containerId, message, type) {
    const el = document.getElementById(containerId);
    if (!el) return;
    el.innerHTML = '<div class="alert alert-' + type + '"><span>' +
        (type === 'error' ? '&#9888;' : '&#10003;') + '</span> ' + message + '</div>';
}

function formatMoney(amount) {
    return 'LKR ' + Number(amount).toLocaleString('en-LK', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function getDashboardPath(user) {
    if (!user) return 'login.html';
    return user.dashboardPath || (user.role === 'ADMIN' ? 'admin-dashboard.html' : 'dashboard.html');
}

function isAdmin(user) {
    return user && user.role === 'ADMIN';
}

async function checkSession(redirectIfLoggedOut) {
    try {
        const result = await apiGet('/session');
        if (result.success) {
            return result.data;
        }
    } catch (e) { /* not logged in */ }

    if (redirectIfLoggedOut) {
        window.location.href = 'login.html';
    }
    return null;
}

async function requireLogin() {
    const user = await checkSession(true);
    if (user) {
        renderSidebar(user);
    }
    return user;
}

async function requireAdmin() {
    const user = await requireLogin();
    if (user && !isAdmin(user)) {
        window.location.href = getDashboardPath(user);
        return null;
    }
    return user;
}

function renderSidebar(user) {
    const sidebar = document.getElementById('sidebar');
    if (!sidebar) return;

    const page = document.body.dataset.page || '';
    const initial = user.fullName ? user.fullName.charAt(0) : 'U';
    const dashPath = getDashboardPath(user);
    const dashPage = dashPath.replace('.html', '').replace('admin-', 'admin-');

    let adminNav = '';
    if (isAdmin(user)) {
        adminNav = `
            <div class="nav-section-label">Administration</div>
            <a href="manage-staff.html" class="nav-item ${page === 'staff' ? 'active' : ''}">
                <span class="nav-icon">&#128101;</span> Manage Staff
            </a>
            <a href="dentists.html" class="nav-item ${page === 'dentists' ? 'active' : ''}">
                <span class="nav-icon">&#129463;</span> Manage Dentists
            </a>`;
    }

    sidebar.innerHTML = `
        <div class="sidebar-brand">
            <h2>Sunrise Dental Clinic</h2>
            <span>Colombo, Sri Lanka</span>
        </div>
        <nav class="sidebar-nav">
            <a href="${dashPath}" class="nav-item ${page === 'dashboard' || page === 'admin-dashboard' ? 'active' : ''}">
                <span class="nav-icon">&#9632;</span> Dashboard
            </a>
            <a href="register.html" class="nav-item ${page === 'register' ? 'active' : ''}">
                <span class="nav-icon">&#10010;</span> Register Appointment
            </a>
            <a href="view-appointment.html" class="nav-item ${page === 'view' ? 'active' : ''}">
                <span class="nav-icon">&#128269;</span> View Appointment
            </a>
            <a href="appointments.html" class="nav-item ${page === 'list' ? 'active' : ''}">
                <span class="nav-icon">&#9776;</span> All Appointments
            </a>
            <a href="bill.html" class="nav-item ${page === 'bill' ? 'active' : ''}">
                <span class="nav-icon">&#128176;</span> Calculate Bill
            </a>
            ${adminNav}
            <a href="help.html" class="nav-item ${page === 'help' ? 'active' : ''}">
                <span class="nav-icon">&#10067;</span> Help
            </a>
        </nav>
        <div class="sidebar-footer">
            <div class="user-info">
                <div class="user-avatar">${initial}</div>
                <div class="user-details">
                    <div class="name">${user.fullName}</div>
                    <div class="role">${user.role}</div>
                </div>
            </div>
            <button onclick="logout()" class="btn btn-danger btn-block btn-sm">Logout</button>
        </div>`;
}

async function logout() {
    await apiPost('/logout', {});
    window.location.href = 'login.html';
}

function renderAppointmentDetails(container, appt) {
    container.innerHTML = `
        <div class="card">
            <div style="text-align:center; margin-bottom:24px;">
                <span class="appointment-badge">${appt.appointmentNumber}</span>
            </div>
            <div class="detail-grid">
                <div class="detail-item"><div class="label">Patient Name</div><div class="value">${appt.patientName}</div></div>
                <div class="detail-item"><div class="label">Contact Number</div><div class="value">${appt.contactNumber}</div></div>
                <div class="detail-item full-width"><div class="label">Address</div><div class="value">${appt.address}</div></div>
                <div class="detail-item"><div class="label">Dentist</div><div class="value">${appt.dentistName}</div></div>
                <div class="detail-item"><div class="label">Treatment Type</div><div class="value">${appt.treatmentType}</div></div>
                <div class="detail-item"><div class="label">Appointment Date</div><div class="value">${appt.appointmentDate}</div></div>
                <div class="detail-item"><div class="label">Appointment Time</div><div class="value">${appt.appointmentTime}</div></div>
            </div>
            <div class="form-actions">
                <a href="bill.html?number=${appt.appointmentNumber}" class="btn btn-primary">Calculate Bill</a>
                <a href="register.html" class="btn btn-secondary">New Appointment</a>
            </div>
        </div>`;
}

function renderAppointmentResults(container, appointments, onSelect) {
    if (!appointments || appointments.length === 0) {
        container.innerHTML = '<div class="alert alert-info"><span>&#9432;</span> No appointments matched your search.</div>';
        return;
    }

    if (appointments.length === 1 && onSelect) {
        renderAppointmentDetails(container, appointments[0]);
        return;
    }

    const cards = appointments.map(appt => `
        <div class="result-card" data-number="${appt.appointmentNumber}">
            <div class="result-card-header">
                <span class="appointment-badge">${appt.appointmentNumber}</span>
                <span class="result-date">${appt.appointmentDate} at ${appt.appointmentTime}</span>
            </div>
            <div class="result-card-body">
                <strong>${appt.patientName}</strong>
                <span>${appt.contactNumber}</span>
                <span>${appt.dentistName} &mdash; ${appt.treatmentType}</span>
            </div>
            <button type="button" class="btn btn-secondary btn-sm view-result-btn">View Details</button>
        </div>`).join('');

    container.innerHTML = `<div class="result-list">${cards}</div>`;

    container.querySelectorAll('.view-result-btn').forEach((btn, index) => {
        btn.addEventListener('click', () => {
            if (onSelect) {
                onSelect(appointments[index]);
            } else {
                renderAppointmentDetails(container, appointments[index]);
            }
        });
    });
}

function calculateBillPreview(bill, consultationFee, discountType, discountValue) {
    const treatmentCost = bill.treatmentCost;
    const fee = Number(consultationFee) || 0;
    const subtotal = treatmentCost + fee;
    let discountAmount = 0;

    if (discountType === 'PERCENT') {
        discountAmount = subtotal * (Math.min(Number(discountValue) || 0, 100) / 100);
    } else if (discountType === 'FIXED') {
        discountAmount = Math.min(Number(discountValue) || 0, subtotal);
    }

    return {
        subtotal,
        discountAmount,
        total: Math.max(0, subtotal - discountAmount)
    };
}

function renderBillEditor(container, bill, onSave) {
    const appt = bill.appointment;
    let consultationFee = bill.consultationFee;
    let discountType = bill.discountType || 'NONE';
    let discountValue = bill.discountValue || 0;

    function render() {
        const preview = calculateBillPreview(bill, consultationFee, discountType, discountValue);
        container.innerHTML = `
            <div class="card bill-editor">
                <h2 class="card-title">Bill for ${appt.appointmentNumber} &mdash; ${appt.patientName}</h2>
                <div class="detail-grid" style="margin-bottom:24px;">
                    <div class="detail-item"><div class="label">Dentist</div><div class="value">${appt.dentistName}</div></div>
                    <div class="detail-item"><div class="label">Treatment</div><div class="value">${appt.treatmentType}</div></div>
                    <div class="detail-item"><div class="label">Treatment Cost</div><div class="value">${formatMoney(bill.treatmentCost)}</div></div>
                    <div class="detail-item"><div class="label">Date</div><div class="value">${appt.appointmentDate}</div></div>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label>Consultation Fee (LKR)</label>
                        <input type="number" id="consultationFee" class="form-control" min="0" step="0.01" value="${consultationFee}">
                    </div>
                    <div class="form-group">
                        <label>Discount Type</label>
                        <select id="discountType" class="form-control">
                            <option value="NONE" ${discountType === 'NONE' ? 'selected' : ''}>No Discount</option>
                            <option value="PERCENT" ${discountType === 'PERCENT' ? 'selected' : ''}>Percentage (%)</option>
                            <option value="FIXED" ${discountType === 'FIXED' ? 'selected' : ''}>Fixed Amount (LKR)</option>
                        </select>
                    </div>
                </div>
                <div class="form-group" id="discountValueGroup" style="${discountType === 'NONE' ? 'display:none;' : ''}">
                    <label id="discountValueLabel">Discount Value</label>
                    <input type="number" id="discountValue" class="form-control" min="0" step="0.01" value="${discountValue}">
                </div>
                <div class="bill-summary">
                    <div class="receipt-row"><span>Subtotal</span><span>${formatMoney(preview.subtotal)}</span></div>
                    <div class="receipt-row"><span>Discount</span><span>- ${formatMoney(preview.discountAmount)}</span></div>
                    <div class="receipt-row total"><span>Total</span><span>${formatMoney(preview.total)}</span></div>
                </div>
                <div class="form-actions">
                    <button type="button" id="saveBillBtn" class="btn btn-primary">Save Bill</button>
                    <button type="button" id="previewReceiptBtn" class="btn btn-secondary">Preview Receipt</button>
                </div>
            </div>
            <div id="receiptBox"></div>`;

        const feeInput = container.querySelector('#consultationFee');
        const typeSelect = container.querySelector('#discountType');
        const valueInput = container.querySelector('#discountValue');
        const valueGroup = container.querySelector('#discountValueGroup');
        const valueLabel = container.querySelector('#discountValueLabel');

        function refreshPreview() {
            consultationFee = feeInput.value;
            discountType = typeSelect.value;
            discountValue = valueInput.value;
            valueGroup.style.display = discountType === 'NONE' ? 'none' : '';
            valueLabel.textContent = discountType === 'PERCENT' ? 'Discount Percentage (%)' : 'Discount Amount (LKR)';
            const next = calculateBillPreview(bill, consultationFee, discountType, discountValue);
            container.querySelector('.bill-summary').innerHTML = `
                <div class="receipt-row"><span>Subtotal</span><span>${formatMoney(next.subtotal)}</span></div>
                <div class="receipt-row"><span>Discount</span><span>- ${formatMoney(next.discountAmount)}</span></div>
                <div class="receipt-row total"><span>Total</span><span>${formatMoney(next.total)}</span></div>`;
        }

        feeInput.addEventListener('input', refreshPreview);
        typeSelect.addEventListener('change', refreshPreview);
        valueInput.addEventListener('input', refreshPreview);

        container.querySelector('#saveBillBtn').addEventListener('click', async () => {
            if (onSave) {
                await onSave({
                    appointmentNumber: appt.appointmentNumber,
                    consultationFee: Number(feeInput.value),
                    discountType: typeSelect.value,
                    discountValue: Number(valueInput.value)
                });
            }
        });

        container.querySelector('#previewReceiptBtn').addEventListener('click', () => {
            const previewBill = {
                ...bill,
                consultationFee: Number(feeInput.value),
                discountType: typeSelect.value,
                discountValue: Number(valueInput.value),
                discountAmount: calculateBillPreview(bill, feeInput.value, typeSelect.value, valueInput.value).discountAmount,
                totalAmount: calculateBillPreview(bill, feeInput.value, typeSelect.value, valueInput.value).total
            };
            renderBillReceipt(container.querySelector('#receiptBox'), previewBill);
        });
    }

    render();
}

function renderBillReceipt(container, bill) {
    const appt = bill.appointment;
    const discountLine = bill.discountAmount > 0
        ? `<div class="receipt-row"><span>Discount (${bill.discountType === 'PERCENT' ? bill.discountValue + '%' : 'Fixed'})</span><span>- ${formatMoney(bill.discountAmount)}</span></div>`
        : '';

    container.innerHTML = `
        <div class="receipt" id="receipt">
            <div class="receipt-header">
                <h2>Sunrise Dental Clinic</h2>
                <p>Colombo, Sri Lanka &mdash; Patient Bill / Treatment Receipt</p>
            </div>
            <div class="receipt-row"><span>Appointment No.</span><strong>${appt.appointmentNumber}</strong></div>
            <div class="receipt-row"><span>Patient Name</span><span>${appt.patientName}</span></div>
            <div class="receipt-row"><span>Contact</span><span>${appt.contactNumber}</span></div>
            <div class="receipt-row"><span>Dentist</span><span>${appt.dentistName}</span></div>
            <div class="receipt-row"><span>Treatment</span><span>${appt.treatmentType}</span></div>
            <div class="receipt-row"><span>Date</span><span>${appt.appointmentDate}</span></div>
            <hr style="border:none; border-top:1px dashed var(--border); margin:16px 0;">
            <div class="receipt-row"><span>Consultation Fee</span><span>${formatMoney(bill.consultationFee)}</span></div>
            <div class="receipt-row"><span>Treatment Cost (${appt.treatmentType})</span><span>${formatMoney(bill.treatmentCost)}</span></div>
            ${discountLine}
            <div class="receipt-row total"><span>TOTAL AMOUNT</span><span>${formatMoney(bill.totalAmount)}</span></div>
            ${bill.issuedByName ? `<div class="receipt-row"><span>Issued By</span><span>${bill.issuedByName}</span></div>` : ''}
            <div class="receipt-footer">
                Thank you for choosing Sunrise Dental Clinic!<br>
                Please keep this receipt for your records.
            </div>
        </div>
        <div class="print-actions">
            <button onclick="window.print()" class="btn btn-primary">Print Receipt</button>
            <a href="${getDashboardPath({ role: 'STAFF' })}" class="btn btn-secondary" id="receiptBackBtn">Back to Dashboard</a>
        </div>`;
}
