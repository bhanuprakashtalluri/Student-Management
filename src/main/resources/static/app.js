(function(){
  const nav = document.getElementById('main-nav');
  const sections = document.querySelectorAll('.content-section');
  const statusBox = document.getElementById('status');
  const yearSpan = document.getElementById('year');
  const themeToggle = document.getElementById('themeToggle');
  yearSpan.textContent = new Date().getFullYear();

  // Theme handling
  const THEME_KEY = 'sm_theme';
  function applyTheme(theme){
    document.documentElement.setAttribute('data-theme', theme);
    if(theme === 'dark') {
      document.documentElement.classList.add('manual-dark');
    } else {
      document.documentElement.classList.remove('manual-dark');
    }
    themeToggle.textContent = theme === 'dark' ? '🌞 Light' : '🌗 Dark';
  }
  function initTheme(){
    const saved = localStorage.getItem(THEME_KEY);
    if(saved){ applyTheme(saved); return; }
    // fallback to system
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    applyTheme(prefersDark ? 'dark' : 'light');
  }
  themeToggle?.addEventListener('click', () => {
    const current = document.documentElement.getAttribute('data-theme')==='dark' ? 'dark':'light';
    const next = current === 'dark' ? 'light' : 'dark';
    localStorage.setItem(THEME_KEY, next);
    applyTheme(next);
  });
  initTheme();

  function showStatus(msg,type){
    statusBox.textContent = msg;
    statusBox.className = 'status ' + (type||'info');
    if(!msg){statusBox.className='status hidden';}
  }

  function activateButton(btn){
    nav.querySelectorAll('button').forEach(b=>b.classList.remove('active'));
    if(btn) btn.classList.add('active');
  }

  function showSection(id){
    sections.forEach(sec=>{
      if(sec.id===id) sec.classList.remove('hidden'); else sec.classList.add('hidden');
    });
  }

  // INSERT: endpoints map + prefetch helper BEFORE any usage
  const sectionEndpoints = {
    students: '/api/students',
    courses: '/api/courses',
    enrollments: '/api/enrollments',
    grades: '/api/grades',
    attendance: '/api/attendance',
    addresses: '/api/addresses',
    contacts: '/api/contacts'
  };
  function prefetchAllSections(){
    Object.entries(sectionEndpoints).forEach(([section, endpoint]) => {
      if(section === 'students') return; // students fetched explicitly
      fetchData(endpoint, section);
    });
  }

  nav.addEventListener('click',e=>{
    if(e.target.tagName==='BUTTON' && e.target.id !== 'themeToggle'){
      const id = e.target.getAttribute('data-section');
      activateButton(e.target);
      showSection(id);
      // Auto-load if cache for that section is empty
      const needLoad = (
        (id==='students' && (!Array.isArray(studentDataCache) || !studentDataCache.length)) ||
        (id==='courses' && (!Array.isArray(courseDataCache) || !courseDataCache.length)) ||
        (id==='enrollments' && (!Array.isArray(enrollmentDataCache) || !enrollmentDataCache.length)) ||
        (id==='grades' && (!Array.isArray(gradeDataCache) || !gradeDataCache.length)) ||
        (id==='attendance' && (!Array.isArray(attendanceDataCache) || !attendanceDataCache.length)) ||
        (id==='addresses' && (!Array.isArray(addressDataCache) || !addressDataCache.length)) ||
        (id==='contacts' && (!Array.isArray(contactDataCache) || !contactDataCache.length))
      );
      if(needLoad && sectionEndpoints[id]) {
        fetchData(sectionEndpoints[id], id);
      }
      showStatus('', '');
    }
  });

  // Generic fetch and render
  async function fetchData(endpoint, section){
    showStatus('Loading '+endpoint+' ...','info');
    try {
      const res = await fetch(endpoint);
      if(!res.ok){ throw new Error(res.status+' '+res.statusText); }
      const data = await res.json();
      renderData(section, data);
      showStatus('Loaded '+(Array.isArray(data)?data.length:0)+' items from '+endpoint,'info');
    } catch(err){
      console.error(err);
      showStatus('Error: '+err.message,'error');
      renderData(section, []);
    }
  }

  // Updated mappings for integer codes to strings
  const genderMap = { 0: 'Male', 1: 'Female', 2: 'Other' };
  const statusMap = { 0: 'Active', 1: 'Inactive', 2: 'Dropout' };
  const attendanceStatusMap = { 0: 'Present', 1: 'Absent', 2: 'Excused' };

  function getGenderString(val) {
    if (typeof val === 'string') return val;
    return genderMap[val] ?? val;
  }
  function getStatusString(val) {
    if (typeof val === 'string') return val;
    return statusMap[val] ?? val;
  }
  function getAttendanceStatusString(val) {
    if (typeof val === 'string') return val;
    return attendanceStatusMap[val] ?? val;
  }

  function renderData(sectionId, data){
    const section = document.getElementById(sectionId);
    if(!section) return;
    const tbody = section.querySelector('tbody');
    if(!tbody) return;
    tbody.innerHTML='';
    if(sectionId === 'students') {
      // Removed indexSelect population block
    }
    if(!Array.isArray(data) || data.length===0){
      const tr=document.createElement('tr');
      const td=document.createElement('td');
      td.colSpan=6; td.textContent='No records';
      tr.appendChild(td); tbody.appendChild(tr); return;
    }
    data.forEach(item=>{
      const tr=document.createElement('tr');
      switch(sectionId){
        case 'students':
          addCells(tr,[item.studentNumber,item.firstName,item.lastName,item.dateOfBirth,getGenderString(item.gender),getStatusString(item.studentStatus)]);
          break;
        case 'enrollments':
          addCells(tr,[item.enrollmentNumber,(item.student? item.student.firstName+' '+item.student.lastName:''),(item.course? item.course.courseName:''), item.enrollmentDate, item.semester, item.overallGrade, item.instructorName]);
          break;
        case 'grades':
          addCells(tr,[
            item.enrollment && item.enrollment.enrollmentNumber? item.enrollment.enrollmentNumber: '',
            (item.enrollment && item.enrollment.student? item.enrollment.student.firstName+' '+item.enrollment.student.lastName:''),
            nested(item,'enrollment','enrollmentNumber'),
            item.assessmentType,
            item.assessmentDate,
            item.obtainedScore,
            item.maxScore,
            item.gradeCode
          ]);
          break;
        case 'attendance':
          addCells(tr,[
            item.enrollment && item.enrollment.enrollmentNumber? item.enrollment.enrollmentNumber: '',
            (item.student? item.student.firstName+' '+item.student.lastName:''),
            item.attendanceDate,
            getAttendanceStatusString(item.attendanceStatus),
            item.semester
          ]);
          break;
        case 'addresses':
          addCells(tr,[item.student? item.student.studentNumber:'', (item.student? item.student.firstName+' '+item.student.lastName:''), item.street, item.city, item.state, item.zipCode]);
          break;
        case 'contacts':
          addCells(tr,[item.student? item.student.studentNumber:'', (item.student? item.student.firstName+' '+item.student.lastName:''), item.emailAddress, item.mobileNumber]);
          break;
        case 'courses':
          addCells(tr,[item.courseName,item.courseCode,item.courseCredits]);
          break;
      }
      // Actions column
      if(sectionId==='students'){
        const key = item.studentNumber; // replaced studentNumericId || studentId
        const td = document.createElement('td');
        td.innerHTML = `<button class="edit-student-btn" data-key="${key}">Edit</button> <button class="delete-student-btn" data-key="${key}">Delete</button>`;
        tr.appendChild(td);
      } else {
        const td = document.createElement('td');
        // Determine id field
        let idField = item.gradeNumber || item.attendanceNumber || item.addressNumber || item.contactNumber || item.enrollmentNumber || item.courseNumber; // numeric fields only
        td.innerHTML = `<button class="edit-generic-btn" data-section="${sectionId}" data-id="${idField}">Edit</button> <button class="delete-generic-btn" data-section="${sectionId}" data-id="${idField}">Delete</button>`;
        tr.appendChild(td);
      }
      tbody.appendChild(tr);
    });
    // Attach listeners for non-student sections
    setTimeout(()=>{
      tbody.querySelectorAll('.delete-generic-btn').forEach(btn=> btn.addEventListener('click', handleGenericDelete));
      tbody.querySelectorAll('.edit-generic-btn').forEach(btn=> btn.addEventListener('click', handleGenericEdit));
    },0);
  }

  function nested(obj, key, prop){
    if(!obj || !obj[key]) return '';
    return obj[key][prop] ?? '';
  }

  function addCells(tr, arr){
    arr.forEach(v=>{ const td=document.createElement('td'); td.textContent=v==null?'':v; tr.appendChild(td); });
  }

  document.querySelectorAll('.refresh').forEach(btn=>{
    btn.addEventListener('click', ()=>{
      const section = btn.closest('section').id;
      const endpoint = btn.getAttribute('data-endpoint');
      fetchData(endpoint, section);
    });
  });

  // CSV upload handler
  const uploadForm = document.getElementById('studentUploadForm');
  uploadForm?.addEventListener('submit', async e => {
    e.preventDefault();
    const fileInput = document.getElementById('studentCsv');
    if(!fileInput.files.length){ showStatus('Select a CSV file first','warn'); return; }
    const formData = new FormData();
    formData.append('file', fileInput.files[0]);
    showStatus('Uploading students CSV...','info');
    try {
      const res = await fetch('/api/students/upload-csv', { method: 'POST', body: formData });
      if(!res.ok){ throw new Error(res.status+' '+res.statusText); }
      const result = await res.json().catch(()=>null);
      showStatus('Upload complete','info');
      // refresh list
      fetchData('/api/students', 'students');
    } catch(err){
      console.error(err);
      showStatus('Upload failed: '+err.message,'error');
    }
  });

  document.querySelectorAll('form.upload-form').forEach(form => {
    form.addEventListener('submit', async e => {
      e.preventDefault();
      const endpoint = form.getAttribute('data-endpoint');
      const fileInput = form.querySelector('input[type="file"]');
      if(!fileInput.files.length){ showStatus('Select a CSV file first','warn'); return; }
      const formData = new FormData(); formData.append('file', fileInput.files[0]);
      showStatus('Uploading CSV to '+endpoint+' ...','info');
      try {
        const res = await fetch(endpoint, { method: 'POST', body: formData });
        const json = await res.json().catch(()=>null);
        if(!res.ok) throw new Error((json?.message)||res.status+' '+res.statusText);
        showStatus('Upload complete ('+endpoint+')','info');
        // trigger refresh button inside same section if present
        const section = form.closest('section');
        const refreshBtn = section?.querySelector('button.refresh');
        if(refreshBtn) refreshBtn.click(); else fetchData(endpoint.replace('/upload-csv',''), section.id);
      } catch(err){
        showStatus('Upload failed: '+err.message,'error');
      }
    });
  });

  // --- STUDENT SEARCH, UPDATE, DELETE ---
  let studentDataCache = [];

  // Search handler
  document.getElementById('studentSearchBtn').addEventListener('click', () => {
    const query = document.getElementById('studentSearchInput').value.trim().toLowerCase();
    if (!query) {
      renderData('students', studentDataCache);
      showStatus('Showing all students', 'info');
      return;
    }
    const filtered = studentDataCache.filter(s =>
      (s.firstName && s.firstName.toLowerCase().includes(query)) ||
      (s.lastName && s.lastName.toLowerCase().includes(query)) ||
      (s.dateOfBirth && s.dateOfBirth.toLowerCase().includes(query)) ||
      (getGenderString(s.gender).toLowerCase() === query) ||
      (getStatusString(s.studentStatus).toLowerCase() === query)
    );
    renderData('students', filtered);
    showStatus('Found ' + filtered.length + ' students for "' + query + '"', 'info');
  });

  // Override fetchData for students to cache data
  const origFetchData = fetchData;
  fetchData = async function(endpoint, section) {
    if (section === 'students') {
      showStatus('Loading students ...','info');
      try {
        const res = await fetch(endpoint);
        if(!res.ok){ throw new Error(res.status+' '+res.statusText); }
        const data = await res.json();
        studentDataCache = data;
        renderData(section, data);
        showStatus('Loaded '+(Array.isArray(data)?data.length:0)+' students','info');
      } catch(err){
        console.error(err);
        showStatus('Error: '+err.message,'error');
        renderData(section, []);
      }
    } else {
      origFetchData(endpoint, section);
    }
  }

  // Render Edit/Delete buttons for students
  const origRenderData = renderData;
  // Helper: get student unique key (use studentNumericId only)
  function getStudentKey(student) {
    return student.studentNumber;
  }
  renderData = function(sectionId, data) {
    if(sectionId !== 'students') return origRenderData(sectionId, data);
    const section = document.getElementById(sectionId);
    if(!section) return;
    const tbody = section.querySelector('tbody');
    if(!tbody) return;
    tbody.innerHTML='';
    if(!Array.isArray(data) || data.length===0){
      const tr=document.createElement('tr');
      const td=document.createElement('td');
      td.colSpan=7; td.textContent='No records';
      tr.appendChild(td); tbody.appendChild(tr); return;
    }
    data.forEach(item=>{
      const tr=document.createElement('tr');
      addCells(tr,[item.studentNumber,item.firstName,item.lastName,item.dateOfBirth,getGenderString(item.gender),getStatusString(item.studentStatus)]);
      const key = getStudentKey(item); // removed fallbacks
      const td = document.createElement('td');
      td.innerHTML = `<button class="edit-student-btn" data-key="${key}">Edit</button> <button class="delete-student-btn" data-key="${key}">Delete</button>`;
      tr.appendChild(td);
      tbody.appendChild(tr);
    });
    setTimeout(() => {
      tbody.querySelectorAll('.edit-student-btn').forEach(btn => btn.addEventListener('click', handleEditStudent));
      tbody.querySelectorAll('.delete-student-btn').forEach(btn => btn.addEventListener('click', handleDeleteStudent));
    }, 0);
  }

  // Edit student handler (inline editing for simplicity)
  async function handleEditStudent(e) {
    const key = e.target.getAttribute('data-key');
    console.log('Edit handler: data-key attribute:', key);
    console.log('studentDataCache:', studentDataCache);
    const student = studentDataCache.find(s => getStudentKey(s) == key);
    console.log('Edit handler: found student:', student);
    if (!student) return;
    const tr = e.target.closest('tr');
    if (!tr) return;
    tr.innerHTML = '';
    ['firstName','lastName','dateOfBirth','gender','studentStatus'].forEach(field => {
      const td = document.createElement('td');
      const input = document.createElement('input');
      input.value = student[field] || '';
      input.name = field;
      if(field === 'dateOfBirth') input.type = 'date';
      td.appendChild(input);
      tr.appendChild(td);
    });
    // Actions: Save/Cancel
    const td = document.createElement('td');
    td.innerHTML = `<button class="save-student-btn" data-key="${key}">Save</button> <button class="cancel-edit-btn">Cancel</button>`;
    tr.appendChild(td);
    tr.querySelector('.save-student-btn').addEventListener('click', async (ev) => {
      const inputs = tr.querySelectorAll('input');
      // Only send changed fields for PATCH
      const updated = {};
      inputs.forEach(inp => {
        let original = student[inp.name];
        let value = inp.value;
        if(inp.name === 'gender' || inp.name === 'studentStatus') {
          value = isNaN(value) ? value : parseInt(value, 10);
        }
        if (value != original) {
          updated[inp.name] = value;
        }
      });
      if (Object.keys(updated).length === 0) {
        renderData('students', studentDataCache);
        return;
      }
      try {
        const res = await fetch(`/api/students/${student.studentNumber}`, {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(updated)
        });
        if (!res.ok) throw new Error(res.status+' '+res.statusText);
        showStatus('Student updated','info');
        fetchData('/api/students','students');
      } catch(err) {
        showStatus('Update failed: '+err.message,'error');
      }
    });
    tr.querySelector('.cancel-edit-btn').addEventListener('click', () => {
      renderData('students', studentDataCache);
    });
  }

  // Delete student handler
  async function handleDeleteStudent(e) {
    const key = e.target.getAttribute('data-key');
    console.log('Delete handler: data-key attribute:', key);
    console.log('studentDataCache:', studentDataCache);
    if (!key) {
      showStatus('Invalid student key for delete','error');
      console.error('Delete error: invalid student key', key);
      return;
    }
    if (!confirm('Delete this student?')) return;
    try {
      console.log('Deleting student key', key);
      const res = await fetch(`/api/students/${key}`, { method: 'DELETE' });
      if (!res.ok) throw new Error(res.status+' '+res.statusText);
      showStatus('Student deleted','info');
      fetchData('/api/students','students');
    } catch(err) {
      showStatus('Delete failed: '+err.message,'error');
    }
  }

  // --- SEARCH FOR ALL PAGES ---
  let courseDataCache = [], enrollmentDataCache = [], gradeDataCache = [], attendanceDataCache = [], addressDataCache = [], contactDataCache = [];

  document.getElementById('courseSearchBtn').addEventListener('click', () => {
    const query = document.getElementById('courseSearchInput').value.trim().toLowerCase();
    if (!query) {
      renderData('courses', courseDataCache);
      showStatus('Showing all courses', 'info');
      return;
    }
    const filtered = courseDataCache.filter(c =>
      (c.courseName && c.courseName.toLowerCase().includes(query)) ||
      (c.courseCode && c.courseCode.toLowerCase().includes(query))
    );
    renderData('courses', filtered);
    showStatus('Found ' + filtered.length + ' courses for "' + query + '"', 'info');
  });

  document.getElementById('enrollmentSearchBtn').addEventListener('click', () => {
    const query = document.getElementById('enrollmentSearchInput').value.trim().toLowerCase();
    if (!query) {
      renderData('enrollments', enrollmentDataCache);
      showStatus('Showing all enrollments', 'info');
      return;
    }
    const filtered = enrollmentDataCache.filter(e =>
      (e.student && ((e.student.firstName && e.student.firstName.toLowerCase().includes(query)) || (e.student.lastName && e.student.lastName.toLowerCase().includes(query)))) ||
      (e.course && e.course.courseName && e.course.courseName.toLowerCase().includes(query)) ||
      (e.semester && e.semester.toLowerCase().includes(query))
    );
    renderData('enrollments', filtered);
    showStatus('Found ' + filtered.length + ' enrollments for "' + query + '"', 'info');
  });

  document.getElementById('gradeSearchBtn').addEventListener('click', () => {
    const query = document.getElementById('gradeSearchInput').value.trim().toLowerCase();
    if (!query) {
      renderData('grades', gradeDataCache);
      showStatus('Showing all grades', 'info');
      return;
    }
    const filtered = gradeDataCache.filter(g =>
      // Search by student name (first or last)
      (g.enrollment && g.enrollment.student && (
        (g.enrollment.student.firstName && g.enrollment.student.firstName.toLowerCase().includes(query)) ||
        (g.enrollment.student.lastName && g.enrollment.student.lastName.toLowerCase().includes(query))
      )) ||
      (g.enrollment && g.enrollment.enrollmentNumber && (g.enrollment.enrollmentNumber+"").toLowerCase().includes(query)) || // replaced enrollmentId
      (g.assessmentType && g.assessmentType.toLowerCase().includes(query)) ||
      (g.gradeCode && (g.gradeCode+"").toLowerCase().includes(query))
    );
    renderData('grades', filtered);
    showStatus('Found ' + filtered.length + ' grades for "' + query + '"', 'info');
  });

  document.getElementById('attendanceSearchBtn').addEventListener('click', () => {
    const query = document.getElementById('attendanceSearchInput').value.trim().toLowerCase();
    if (!query) {
      renderData('attendance', attendanceDataCache);
      showStatus('Showing all attendance records', 'info');
      return;
    }
    // Exact match for attendanceStatus, partial for others
    const filtered = attendanceDataCache.filter(a =>
      (a.student && ((a.student.firstName && a.student.firstName.toLowerCase().includes(query)) || (a.student.lastName && a.student.lastName.toLowerCase().includes(query)))) ||
      (a.attendanceDate && a.attendanceDate.toLowerCase().includes(query)) ||
      (getAttendanceStatusString(a.attendanceStatus).toLowerCase() === query) ||
      (a.semester && a.semester.toLowerCase().includes(query))
    );
    renderData('attendance', filtered);
    showStatus('Found ' + filtered.length + ' attendance records for "' + query + '"', 'info');
  });

  document.getElementById('addressSearchBtn').addEventListener('click', () => {
    const query = document.getElementById('addressSearchInput').value.trim().toLowerCase();
    if (!query) {
      renderData('addresses', addressDataCache);
      showStatus('Showing all addresses', 'info');
      return;
    }
    const filtered = addressDataCache.filter(a =>
      (a.student && ((a.student.firstName && a.student.firstName.toLowerCase().includes(query)) || (a.student.lastName && a.student.lastName.toLowerCase().includes(query)))) ||
      (a.street && a.street.toLowerCase().includes(query)) ||
      (a.city && a.city.toLowerCase().includes(query)) ||
      (a.state && a.state.toLowerCase().includes(query)) ||
      (a.zipCode && a.zipCode.toLowerCase().includes(query))
    );
    renderData('addresses', filtered);
    showStatus('Found ' + filtered.length + ' addresses for "' + query + '"', 'info');
  });

  document.getElementById('contactSearchBtn').addEventListener('click', () => {
    const query = document.getElementById('contactSearchInput').value.trim().toLowerCase();
    if (!query) {
      renderData('contacts', contactDataCache);
      showStatus('Showing all contacts', 'info');
      return;
    }
    const filtered = contactDataCache.filter(c =>
      (c.student && ((c.student.firstName && c.student.firstName.toLowerCase().includes(query)) || (c.student.lastName && c.student.lastName.toLowerCase().includes(query)))) ||
      (c.emailAddress && c.emailAddress.toLowerCase().includes(query)) ||
      (c.mobileNumber && c.mobileNumber.toLowerCase().includes(query))
    );
    renderData('contacts', filtered);
    showStatus('Found ' + filtered.length + ' contacts for "' + query + '"', 'info');
  });

  // Override fetchData for all sections to cache data
  const origFetchData2 = fetchData;
  fetchData = async function(endpoint, section) {
    switch(section) {
      case 'students':
        try {
          const res = await fetch(endpoint);
          if(!res.ok){ throw new Error(res.status+' '+res.statusText); }
          const data = await res.json();
          studentDataCache = data;
          renderData(section, data);
          showStatus('Loaded '+(Array.isArray(data)?data.length:0)+' students','info');
        } catch(err){
          console.error(err);
          showStatus('Error: '+err.message,'error');
          renderData(section, []);
        }
        break;
      case 'courses':
        try {
          const res = await fetch(endpoint);
          if(!res.ok){ throw new Error(res.status+' '+res.statusText); }
          const data = await res.json();
          courseDataCache = data;
          renderData(section, data);
          showStatus('Loaded '+(Array.isArray(data)?data.length:0)+' courses','info');
        } catch(err){
          console.error(err);
          showStatus('Error: '+err.message,'error');
          renderData(section, []);
        }
        break;
      case 'enrollments':
        try {
          const res = await fetch(endpoint);
          if(!res.ok){ throw new Error(res.status+' '+res.statusText); }
          const data = await res.json();
          enrollmentDataCache = data;
          renderData(section, data);
          showStatus('Loaded '+(Array.isArray(data)?data.length:0)+' enrollments','info');
        } catch(err){
          console.error(err);
          showStatus('Error: '+err.message,'error');
          renderData(section, []);
        }
        break;
      case 'grades':
        try {
          const res = await fetch(endpoint);
          if(!res.ok){ throw new Error(res.status+' '+res.statusText); }
          const data = await res.json();
          gradeDataCache = data;
          renderData(section, data);
          showStatus('Loaded '+(Array.isArray(data)?data.length:0)+' grades','info');
        } catch(err){
          console.error(err);
          showStatus('Error: '+err.message,'error');
          renderData(section, []);
        }
        break;
      case 'attendance':
        try {
          const res = await fetch(endpoint);
          if(!res.ok){ throw new Error(res.status+' '+res.statusText); }
          const data = await res.json();
          attendanceDataCache = data;
          renderData(section, data);
          showStatus('Loaded '+(Array.isArray(data)?data.length:0)+' attendance records','info');
        } catch(err){
          console.error(err);
          showStatus('Error: '+err.message,'error');
          renderData(section, []);
        }
        break;
      case 'addresses':
        try {
          const res = await fetch(endpoint);
          if(!res.ok){ throw new Error(res.status+' '+res.statusText); }
          const data = await res.json();
          addressDataCache = data;
          renderData(section, data);
          showStatus('Loaded '+(Array.isArray(data)?data.length:0)+' addresses','info');
        } catch(err){
          console.error(err);
          showStatus('Error: '+err.message,'error');
          renderData(section, []);
        }
        break;
      case 'contacts':
        try {
          const res = await fetch(endpoint);
          if(!res.ok){ throw new Error(res.status+' '+res.statusText); }
          const data = await res.json();
          contactDataCache = data;
          renderData(section, data);
          showStatus('Loaded '+(Array.isArray(data)?data.length:0)+' contacts','info');
        } catch(err){
          console.error(err);
          showStatus('Error: '+err.message,'error');
          renderData(section, []);
        }
        break;
      default:
        origFetchData2(endpoint, section);
    }
  }

  // Default view
  const firstButton = nav.querySelector('button[data-section="students"]');
  activateButton(firstButton); showSection('students');
  // Initial auto-load students plus background prefetch of other sections
  fetchData('/api/students','students');
  prefetchAllSections();

  const addBtn = document.getElementById('addStudentBtn');
  const addForm = document.getElementById('addStudentForm');
  const cancelAdd = document.getElementById('cancelAddStudent');

  function toggleAddStudent(show){
    if(!addForm) return;
    if(show){ addForm.classList.remove('hidden'); } else { addForm.classList.add('hidden'); addForm.reset(); }
  }
  addBtn?.addEventListener('click', ()=> toggleAddStudent(true));
  cancelAdd?.addEventListener('click', ()=> toggleAddStudent(false));

  const addressRowsDiv = document.getElementById('addressRows');
  const contactRowsDiv = document.getElementById('contactRows');
  const enrollmentRowsDiv = document.getElementById('enrollmentRows');
  document.getElementById('addAddressRow')?.addEventListener('click', ()=>{
    const wrap = document.createElement('div');
    wrap.className='addr-row';
    wrap.innerHTML = `<input placeholder="Street" required name="addr_street"/> <input placeholder="City" required name="addr_city"/> <input placeholder="State" required name="addr_state"/> <input placeholder="Zip" required name="addr_zip"/> <button type="button" class="remove-btn">x</button>`;
    wrap.querySelector('.remove-btn').addEventListener('click',()=>wrap.remove());
    addressRowsDiv.appendChild(wrap);
  });
  document.getElementById('addContactRow')?.addEventListener('click', ()=>{
    const wrap = document.createElement('div');
    wrap.className='contact-row';
    wrap.innerHTML = `<input placeholder="Email" type="email" required name="contact_email"/> <input placeholder="Mobile" required name="contact_mobile"/> <button type="button" class="remove-btn">x</button>`;
    wrap.querySelector('.remove-btn').addEventListener('click',()=>wrap.remove());
    contactRowsDiv.appendChild(wrap);
  });
  document.getElementById('addEnrollmentRow')?.addEventListener('click', ()=>{
    const wrap = document.createElement('div');
    wrap.className='enrollment-row';
    wrap.innerHTML = `<input placeholder="Course #" type="number" required name="en_course" style="width:90px;"/> <input placeholder="Enroll Date" type="date" required name="en_date"/> <input placeholder="Overall Grade" type="number" required name="en_grade" style="width:110px;"/> <input placeholder="Semester" required name="en_sem" style="width:100px;"/> <input placeholder="Instructor" required name="en_instr"/> <button type="button" class="remove-btn">x</button>`;
    wrap.querySelector('.remove-btn').addEventListener('click',()=>wrap.remove());
    enrollmentRowsDiv.appendChild(wrap);
  });

  // --- Course list cache for enrollment dropdowns ---
  let cachedCourses = null;
  async function loadCoursesForDropdown() {
    if (cachedCourses) return cachedCourses;
    try {
      const res = await fetch('/api/courses');
      if(!res.ok) throw new Error('Failed courses: '+res.status);
      const data = await res.json();
      // Normalize: expect array of course objects with courseNumber & courseName & courseCode
      cachedCourses = Array.isArray(data) ? data : [];
    } catch(err) {
      console.error('Course load error', err);
      cachedCourses = [];
    }
    return cachedCourses;
  }
  function buildCourseSelectHtml() {
    const courses = cachedCourses || [];
    if(!courses.length) {
      return '<select name="en_course" required><option value="" disabled selected>No courses</option></select>';
    }
    const opts = courses.map(c => `<option value="${c.courseNumber}">${c.courseName} (${c.courseCode})</option>`).join('');
    return `<select name="en_course" required><option value="" disabled selected>Select course</option>${opts}</select>`;
  }
  async function addEnrollmentRowDynamic() {
    await loadCoursesForDropdown();
    const wrap = document.createElement('div');
    wrap.className='enrollment-row';
    wrap.innerHTML = `${buildCourseSelectHtml()} <input placeholder="Enroll Date" type="date" required name="en_date"/> <input placeholder="Overall Grade" type="number" min="0" max="100" required name="en_grade" style="width:110px;"/> <input placeholder="Semester" required name="en_sem" style="width:100px;"/> <input placeholder="Instructor" required name="en_instr"/> <button type="button" class="remove-btn">x</button>`;
    wrap.querySelector('.remove-btn').addEventListener('click',()=>wrap.remove());
    enrollmentRowsDiv.appendChild(wrap);
  }
  // Replace previous addEnrollmentRow listener
  const addEnrollmentRowBtn = document.getElementById('addEnrollmentRow');
  if(addEnrollmentRowBtn){
    addEnrollmentRowBtn.replaceWith(addEnrollmentRowBtn.cloneNode(true));
  }
  document.getElementById('addEnrollmentRow')?.addEventListener('click', () => {
    addEnrollmentRowDynamic();
  });
  // Pre-load courses when opening add student form
  addBtn?.addEventListener('click', () => { loadCoursesForDropdown().then(()=>{}); });

  // Unified submit handler (aggregate-aware)
  addForm?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const fd = new FormData(addForm);
    const coreRequired = ['firstName','lastName','dateOfBirth','gender','joiningDate','studentStatus'];
    const base = {};
    // FIXED loop: previously missing closing brace and assignment unreachable
    for (const f of coreRequired) {
      const v = fd.get(f);
      if (!v) {
        showStatus('Please add all the details','error');
        return;
      }
      base[f] = (f === 'gender' || f === 'studentStatus') ? parseInt(v,10) : v;
    }
    // Gather optional sets
    const addressRows = Array.from(document.querySelectorAll('#addressRows .addr-row'));
    const contactRows = Array.from(document.querySelectorAll('#contactRows .contact-row'));
    const enrollmentRows = Array.from(document.querySelectorAll('#enrollmentRows .enrollment-row'));
    const addresses = addressRows.map(r=>({
      street:r.querySelector('[name=addr_street]').value.trim(),
      city:r.querySelector('[name=addr_city]').value.trim(),
      state:r.querySelector('[name=addr_state]').value.trim(),
      zipCode:r.querySelector('[name=addr_zip]').value.trim()
    })).filter(a=>a.street && a.city && a.state && a.zipCode);
    const contacts = contactRows.map(r=>({
      emailAddress:r.querySelector('[name=contact_email]').value.trim(),
      mobileNumber:r.querySelector('[name=contact_mobile]').value.trim()
    })).filter(c=>c.emailAddress && c.mobileNumber);
    const enrollments = enrollmentRows.map(r=>({
      courseNumber: parseInt(r.querySelector('[name=en_course]').value,10),
      enrollmentDate: r.querySelector('[name=en_date]').value,
      overallGrade: parseInt(r.querySelector('[name=en_grade]').value,10),
      semester: r.querySelector('[name=en_sem]').value.trim(),
      instructorName: r.querySelector('[name=en_instr]').value.trim()
    })).filter(en=>en.courseNumber && en.enrollmentDate && en.semester && en.instructorName && !isNaN(en.overallGrade));

    const useAggregate = addresses.length || contacts.length || enrollments.length;
    const endpoint = useAggregate? '/api/students/aggregate':'/api/students';
    const payload = useAggregate? { ...base, addresses, contacts, enrollments } : base;
    try {
      showStatus('Creating student...','info');
      const res = await fetch(endpoint, { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(payload)});
      if(!res.ok){
        let msg = res.status+' '+res.statusText;
        try { const j = await res.json(); if(j.message) msg = j.message; } catch(_){ }
        throw new Error(msg);
      }
      showStatus('Student added','info');
      addForm.classList.add('hidden'); addForm.reset();
      fetchData('/api/students','students');
    } catch(err){
      showStatus('Create failed: '+err.message,'error');
    }
  });

  // Helper to toggle generic add forms
  function toggleForm(id, show){
    const f = document.getElementById(id);
    if(!f) return; if(show){ f.classList.remove('hidden'); } else { f.classList.add('hidden'); f.reset(); }
  }
  // Show buttons mapping
  const showButtons = [
    ['showAddCourse','addCourseForm'],
    ['showAddEnrollment','addEnrollmentForm'],
    ['showAddGrade','addGradeForm'],
    ['showAddAttendance','addAttendanceForm'],
    ['showAddAddress','addAddressForm'],
    ['showAddContact','addContactForm']
  ];
  showButtons.forEach(([btnId, formId])=>{
    const b = document.getElementById(btnId);
    if(b){ b.addEventListener('click', ()=> toggleForm(formId,true)); }
  });
  // Cancel buttons (data-cancel attribute)
  document.querySelectorAll('.cancel-add').forEach(btn=>{
    btn.addEventListener('click', ()=>{
      const type = btn.getAttribute('data-cancel');
      const formId = 'add'+ type.charAt(0).toUpperCase()+type.slice(1)+'Form';
      toggleForm(formId,false);
    });
  });
  // Generic JSON POST
  async function postJson(url, payload){
    const res = await fetch(url,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(payload)});
    let body=null; try{ body=await res.json(); }catch(_){ }
    if(!res.ok){ const msg = body?.message || (res.status+' '+res.statusText); throw new Error(msg); }
    return body;
  }
  // Validation util
  function requireFields(obj, fields){
    for(const f of fields){ if(obj[f]===undefined || obj[f]===null || obj[f]==='' ){ return f; } }
    return null;
  }
  // Course form
  const courseForm = document.getElementById('addCourseForm');
  courseForm?.addEventListener('submit', async e => {
    e.preventDefault(); const fd=new FormData(courseForm);
    const payload={ courseName:fd.get('courseName').trim(), courseCode:fd.get('courseCode').trim(), courseCredits: parseFloat(fd.get('courseCredits')) };
    const missing = requireFields(payload,['courseName','courseCode','courseCredits']);
    if(missing){ showStatus('Missing '+missing,'error'); return; }
    if(isNaN(payload.courseCredits)){ showStatus('Invalid credits','error'); return; }
    try { showStatus('Saving course...','info'); await postJson('/api/courses',payload); showStatus('Course added','info'); toggleForm('addCourseForm',false); fetchData('/api/courses','courses'); } catch(err){ showStatus('Course add failed: '+err.message,'error'); }
  });
  // Enrollment form
  const enrollmentForm = document.getElementById('addEnrollmentForm');
  enrollmentForm?.addEventListener('submit', async e => {
    e.preventDefault(); const fd=new FormData(enrollmentForm);
    const payload={ studentNumber:fd.get('studentNumber').trim(), courseNumber:fd.get('courseNumber').trim(), enrollmentDate:fd.get('enrollmentDate'), overallGrade:parseInt(fd.get('overallGrade'),10), semester:fd.get('semester').trim(), instructorName:fd.get('instructorName').trim() };
    const missing = requireFields(payload,['studentNumber','courseNumber','enrollmentDate','overallGrade','semester','instructorName']);
    if(missing){ showStatus('Missing '+missing,'error'); return; }
    if(isNaN(payload.overallGrade) || payload.overallGrade<0 || payload.overallGrade>100){ showStatus('Overall grade must be 0-100','error'); return; }
    if(!(await ensureStudentExists(payload.studentNumber))){ showStatus('Student '+payload.studentNumber+' does not exist','error'); return; }
    if(!(await ensureCourseExists(payload.courseNumber))){ showStatus('Course '+payload.courseNumber+' does not exist','error'); return; }
    try { showStatus('Saving enrollment...','info'); await postJson('/api/enrollments',payload); showStatus('Enrollment added','info'); toggleForm('addEnrollmentForm',false); fetchData('/api/enrollments','enrollments'); } catch(err){ showStatus('Enrollment add failed: '+err.message,'error'); }
  });
  // Grade form
  const gradeForm = document.getElementById('addGradeForm');
  if(gradeForm){ gradeForm.addEventListener('submit', async e => {
    e.preventDefault(); const fd=new FormData(gradeForm);
    const enrollmentNumber = fd.get('enrollmentNumber').trim();
    const rawGradeCode = fd.get('gradeCode');
    const gradeCodeVal = rawGradeCode === '' ? null : (rawGradeCode != null ? parseInt(rawGradeCode,10) : null);
    const payload={ enrollmentNumber: parseInt(enrollmentNumber,10), assessmentDate:fd.get('assessmentDate'), assessmentType:fd.get('assessmentType').trim(), obtainedScore:parseInt(fd.get('obtainedScore'),10), maxScore:parseInt(fd.get('maxScore'),10), gradeCode: gradeCodeVal };
    const missing = ['enrollmentNumber','assessmentDate','assessmentType','obtainedScore','maxScore'].find(k=>!payload[k] && payload[k]!==0);
    if(missing){ showStatus('Missing '+missing,'error'); return; }
    if(!(await ensureEnrollmentNumberExists(enrollmentNumber))){ showStatus('Enrollment number '+enrollmentNumber+' not found','error'); return; }
    if(isNaN(payload.obtainedScore) || payload.obtainedScore<0 || payload.obtainedScore>100){ showStatus('Score 0-100','error'); return; }
    if(isNaN(payload.maxScore) || payload.maxScore<1 || payload.maxScore>100){ showStatus('Max 1-100','error'); return; }
    if(payload.gradeCode!==null && (isNaN(payload.gradeCode) || payload.gradeCode<0 || payload.gradeCode>10)){ showStatus('Grade Code 0-10','error'); return; }
    try { showStatus('Saving grade...','info'); await postJson('/api/grades',payload); showStatus('Grade added','info'); toggleForm('addGradeForm',false); fetchData('/api/grades','grades'); } catch(err){ showStatus('Grade add failed: '+err.message,'error'); }
  }); }
  // Attendance form rewrite
  const attendanceForm2 = document.getElementById('addAttendanceForm');
  if(attendanceForm2){ attendanceForm2.addEventListener('submit', async e => {
    e.preventDefault(); const fd=new FormData(attendanceForm2);
    const studentNumber = fd.get('studentNumber').trim();
    const enrollmentNumber = fd.get('enrollmentNumber').trim();
    const payload={ studentNumber, enrollmentNumber: parseInt(enrollmentNumber,10), attendanceDate:fd.get('attendanceDate'), attendanceStatus: parseInt(fd.get('attendanceStatus'),10), semester: fd.get('semester')? fd.get('semester').trim(): null };
    const missing = ['studentNumber','enrollmentNumber','attendanceDate','attendanceStatus'].find(k=>!payload[k] && payload[k]!==0);
    if(missing){ showStatus('Missing '+missing,'error'); return; }
    if(!(await ensureStudentExists(studentNumber))){ showStatus('Student '+studentNumber+' does not exist','error'); return; }
    if(!(await ensureEnrollmentNumberExists(enrollmentNumber))){ showStatus('Enrollment number '+enrollmentNumber+' not found','error'); return; }
    if(isNaN(payload.attendanceStatus) || payload.attendanceStatus<0 || payload.attendanceStatus>2){ showStatus('Invalid status','error'); return; }
    try { showStatus('Saving attendance...','info'); await postJson('/api/attendance',payload); showStatus('Attendance added','info'); toggleForm('addAttendanceForm',false); fetchData('/api/attendance','attendance'); } catch(err){ showStatus('Attendance add failed: '+err.message,'error'); }
  }); }
  // Update generic delete/edit handlers
  async function handleGenericDelete(e){
    const btn = e.currentTarget; const section = btn.getAttribute('data-section'); const id = btn.getAttribute('data-id');
    if(!id){ showStatus('Missing number','error'); return; }
    if(!confirm('Delete this record?')) return;
    try { const res = await fetch(`/api/${section}/${id}`, { method:'DELETE' }); if(!res.ok) throw new Error(res.status+' '+res.statusText); showStatus('Deleted','info'); fetchData(`/api/${section}`, section); } catch(err){ showStatus('Delete failed: '+err.message,'error'); }
  }
  function handleGenericEdit(e){
    const btn=e.currentTarget; const section=btn.getAttribute('data-section'); const id=Number(btn.getAttribute('data-id'));
    let sourceArr=[]; if(section==='enrollments') sourceArr=enrollmentDataCache; else if(section==='grades') sourceArr=gradeDataCache; else if(section==='attendance') sourceArr=attendanceDataCache; else if(section==='addresses') sourceArr=addressDataCache; else if(section==='contacts') sourceArr=contactDataCache; else if(section==='courses') sourceArr=courseDataCache;
    const item = sourceArr.find(it=> extractNumericId(it, section)===id);
    if(!item){ showStatus('Record not found','error'); return; }
    const tr=btn.closest('tr'); if(!tr) return; replaceRowWithEdit(tr, section, item);
  }
  // Inline edit save uses numeric id
  function replaceRowWithEdit(tr, section, item){
    tr.innerHTML=''; const cells=[]; const append=(name,value,type='text')=>{ const td=document.createElement('td'); const inp=document.createElement('input'); inp.name=name; inp.type=type; inp.value=value==null?'':value; td.appendChild(inp); tr.appendChild(td); cells.push(inp); };
    if(section==='enrollments'){ append('studentNumber', item.student?.studentNumber,'number'); append('courseNumber', item.course?.courseNumber,'number'); append('enrollmentDate', item.enrollmentDate,'date'); append('semester', item.semester); append('overallGrade', item.overallGrade,'number'); append('instructorName', item.instructorName); }
    else if(section==='grades'){ append('enrollmentNumber', item.enrollment?.enrollmentNumber,'number'); append('assessmentType', item.assessmentType); append('assessmentDate', item.assessmentDate,'date'); append('obtainedScore', item.obtainedScore,'number'); append('maxScore', item.maxScore,'number'); append('gradeCode', item.gradeCode,'number'); }
    else if(section==='attendance'){ append('enrollmentNumber', item.enrollment?.enrollmentNumber,'number'); append('studentNumber', item.student?.studentNumber,'number'); append('attendanceDate', item.attendanceDate,'date'); append('attendanceStatus', item.attendanceStatus,'number'); append('semester', item.semester); }
    else if(section==='addresses'){ append('studentNumber', item.student?.studentNumber,'number'); append('street', item.street); append('city', item.city); append('state', item.state); append('zipCode', item.zipCode); }
    else if(section==='contacts'){ append('studentNumber', item.student?.studentNumber,'number'); append('emailAddress', item.emailAddress); append('mobileNumber', item.mobileNumber); }
    else if(section==='courses'){ append('courseName', item.courseName); append('courseCode', item.courseCode); append('courseCredits', item.courseCredits,'number'); }
    const id = extractNumericId(item, section);
    const td=document.createElement('td'); td.innerHTML=`<button class="save-inline-btn" data-section="${section}" data-id="${id}">Save</button> <button class="cancel-inline-btn" data-section="${section}">Cancel</button>`; tr.appendChild(td);
    td.querySelector('.save-inline-btn').addEventListener('click', async ev=>{
      const payload={}; cells.forEach(inp=> payload[inp.name]=inp.value.trim());
      ['studentNumber','courseNumber','enrollmentNumber','overallGrade','obtainedScore','maxScore','gradeCode','attendanceStatus','courseCredits'].forEach(f=>{ if(payload[f]!==undefined && payload[f]!=='' && !isNaN(payload[f])) payload[f]=Number(payload[f]); });
      let endpoint=`/api/${section}/${id}`;
      try { const res=await fetch(endpoint,{method:'PUT',headers:{'Content-Type':'application/json'},body:JSON.stringify(payload)}); if(!res.ok) throw new Error(res.status+' '+res.statusText); showStatus('Updated','info'); fetchData(`/api/${section}`, section); } catch(err){ showStatus('Update failed: '+err.message,'error'); }
    });
    td.querySelector('.cancel-inline-btn').addEventListener('click', ()=> fetchData(`/api/${section}`, section));
  }
  // Replace action key logic to use numeric numbers only
  function getStudentKey(student){ return student.studentNumber; }
  // Update renderData student actions
  // Replace generic actions id field selection to numeric numbers
  function extractNumericId(item, section){
    switch(section){
      case 'enrollments': return item.enrollmentNumber;
      case 'grades': return item.gradeNumber;
      case 'attendance': return item.attendanceNumber;
      case 'addresses': return item.addressNumber;
      case 'contacts': return item.contactNumber;
      case 'courses': return item.courseNumber;
      default: return null;
    }
  }
  // Patch renderData generic actions
  // Find the switch(sectionId) block and replace idField assignment
  // Remove warning about numeric UUID
  // Simplify sets: track enrollmentNumberSet only
  let studentNumberSet = new Set();
  let courseNumberSet = new Set();
  let enrollmentNumberSet = new Set();
  function rebuildSets(section,data){
    if(section==='students' && Array.isArray(data)) studentNumberSet = new Set(data.map(s=>s.studentNumber));
    if(section==='courses' && Array.isArray(data)) courseNumberSet = new Set(data.map(c=>c.courseNumber));
    if(section==='enrollments' && Array.isArray(data)) enrollmentNumberSet = new Set(data.map(e=>e.enrollmentNumber));
  }
  async function ensureStudentExists(num){ if(studentNumberSet.has(Number(num))) return true; await fetchData('/api/students','students'); return studentNumberSet.has(Number(num)); }
  async function ensureCourseExists(num){ if(courseNumberSet.has(Number(num))) return true; await fetchData('/api/courses','courses'); return courseNumberSet.has(Number(num)); }
  async function ensureEnrollmentNumberExists(num){ if(enrollmentNumberSet.has(Number(num))) return true; await fetchData('/api/enrollments','enrollments'); return enrollmentNumberSet.has(Number(num)); }
})();
