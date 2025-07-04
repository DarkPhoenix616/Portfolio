'use strict';



// element toggle function
const elementToggleFunc = function (elem) { elem.classList.toggle("active"); }



// sidebar variables
const sidebar = document.querySelector("[data-sidebar]");
const sidebarBtn = document.querySelector("[data-sidebar-btn]");

// sidebar toggle functionality for mobile
sidebarBtn.addEventListener("click", function () { elementToggleFunc(sidebar); });



// testimonials variables
const testimonialsItem = document.querySelectorAll("[data-testimonials-item]");
const modalContainer = document.querySelector("[data-modal-container]");
const modalCloseBtn = document.querySelector("[data-modal-close-btn]");
const overlay = document.querySelector("[data-overlay]");

// modal variable
const modalImg = document.querySelector("[data-modal-img]");
const modalTitle = document.querySelector("[data-modal-title]");
const modalText = document.querySelector("[data-modal-text]");

// modal toggle function
const testimonialsModalFunc = function () {
  modalContainer.classList.toggle("active");
  overlay.classList.toggle("active");
}

// add click event to all modal items
for (let i = 0; i < testimonialsItem.length; i++) {

  testimonialsItem[i].addEventListener("click", function () {

    modalImg.src = this.querySelector("[data-testimonials-avatar]").src;
    modalImg.alt = this.querySelector("[data-testimonials-avatar]").alt;
    modalTitle.innerHTML = this.querySelector("[data-testimonials-title]").innerHTML;
    modalText.innerHTML = this.querySelector("[data-testimonials-text]").innerHTML;

    testimonialsModalFunc();

  });

}

// add click event to modal close button
modalCloseBtn.addEventListener("click", testimonialsModalFunc);
overlay.addEventListener("click", testimonialsModalFunc);



// custom select variables
const select = document.querySelector("[data-select]");
const selectItems = document.querySelectorAll("[data-select-item]");
const selectValue = document.querySelector("[data-selecct-value]");
const filterBtn = document.querySelectorAll("[data-filter-btn]");

select.addEventListener("click", function () { elementToggleFunc(this); });

// add event in all select items
for (let i = 0; i < selectItems.length; i++) {
  selectItems[i].addEventListener("click", function () {

    let selectedValue = this.innerText.toLowerCase();
    selectValue.innerText = this.innerText;
    elementToggleFunc(select);
    filterFunc(selectedValue);

  });
}

// filter variables
const filterItems = document.querySelectorAll("[data-filter-item]");

const filterFunc = function (selectedValue) {

  for (let i = 0; i < filterItems.length; i++) {

    if (selectedValue === "all") {
      filterItems[i].classList.add("active");
    } else if (selectedValue === filterItems[i].dataset.category) {
      filterItems[i].classList.add("active");
    } else {
      filterItems[i].classList.remove("active");
    }

  }

}

// add event in all filter button items for large screen
let lastClickedBtn = filterBtn[0];

for (let i = 0; i < filterBtn.length; i++) {

  filterBtn[i].addEventListener("click", function () {

    let selectedValue = this.innerText.toLowerCase();
    selectValue.innerText = this.innerText;
    filterFunc(selectedValue);

    lastClickedBtn.classList.remove("active");
    this.classList.add("active");
    lastClickedBtn = this;

  });

}



// utils.js (or at top of your script)
// —————————————————————————————————————————————————————————————
// 1) Robust getCookie (unchanged)
function getCookie(name) {
  const match = document.cookie.match(
      new RegExp('(^| )' + name + '=([^;]+)')
  );
  return match ? match[2] : '';
}

// 2) Improved parseJwt to handle URL‑safe Base64 and padding
function parseJwt(token) {
  if (!token) return null;
  // strip any "Bearer " prefix
  token = token.replace(/^Bearer\s+/, '');
  const parts = token.split('.');
  if (parts.length !== 3) return null;
  let payload = parts[1]
      .replace(/-/g, '+')
      .replace(/_/g, '/');
  // pad with '='
  switch (payload.length % 4) {
    case 2: payload += '=='; break;
    case 3: payload += '=';  break;
  }
  try {
    const json = atob(payload);
    return JSON.parse(json);
  } catch (e) {
    console.error('Invalid JWT payload', e);
    return null;
  }
}

// DOM refs
const form       = document.getElementById('contactForm');
const inputs     = form.querySelectorAll('[data-form-input]');
const formBtn    = form.querySelector('[data-form-btn]');
const oauthBtn   = document.getElementById('google-oauth-btn');
const emailInput = form.querySelector('input[name="email"]');
const statusText = document.getElementById('form-status');
const subjectIn  = form.querySelector('input[name="subject"]');
const messageTa  = form.querySelector('textarea[name="message"]');
// Add this with your other DOM refs
const nameInput = form.querySelector('input[name="fullname"]');

// OAuth flow
oauthBtn.addEventListener('click', () => {
  window.location.href = 'https://portfolio-backend-app-123.azurewebsites.net/oauth2/authorization/google';
});
//window.location.href = 'http://localhost:8080/oauth2/authorization/google';
// --- INITIAL STATE: require sign‑in before allowing subject/message ---
const token = getCookie('DANIES_JWT_TOKEN');
if (token) {
  // user is signed in → fill email + name + enable fields
  const payload = parseJwt(token);
  if (payload) {
    if (payload.email) {
      emailInput.value = payload.email;
    }
    if (payload.name) {  // Check for name in JWT
      nameInput.value = payload.name;
    } else if (payload.given_name) {  // Google OAuth fallback
      nameInput.value = payload.given_name;
    }
    oauthBtn.style.display = 'none';
  }
  // now let them type subject & message
  subjectIn.disabled = false;
  messageTa.disabled = false;

  // and enable the form‑submit listener logic below
  inputs.forEach(input =>
      input.addEventListener('input', () => {
        formBtn.disabled = !form.checkValidity();
      })
  );
} else {
  // not signed in → disable fields
  subjectIn.disabled = true;
  messageTa.disabled = true;
  formBtn.disabled = true;
  nameInput.value = ''; // Clear name if logged out
}


// submission logic
// submission logic
form.addEventListener('submit', async e => {
  e.preventDefault();
  formBtn.disabled = true;
  statusText.textContent = 'Sending...';

  const sub = form.elements.subject.value;
  const msg = form.elements.message.value;
  const name = form.elements.name.value;
  const jwt = getCookie('DANIES_JWT_TOKEN');

  try {
    const res = await fetch(
        'https://portfolio-backend-app-123.azurewebsites.net/api/user/contact/sendMessage',
        {
          method: 'POST',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${jwt}`
          },
          body: JSON.stringify({ subject: sub, message: msg, name: name }),
        }
    );

    if (res.ok) {
      statusText.style.color = 'green';
      statusText.textContent = 'Message sent successfully!';

      // Only reset subject and message (keep email and name)
      form.elements.subject.value = '';
      form.elements.message.value = '';

      // Keep fields enabled and form ready for next message
      formBtn.disabled = !form.checkValidity();
    } else {
      throw new Error(await res.text());
    }
  } catch (err) {
    statusText.style.color = 'crimson';
    statusText.textContent = 'Error: ' + err.message;
    formBtn.disabled = false;

    // Special case: If 401 (Unauthorized), force re-login
    if (err.message.includes('401')) {
      document.cookie = "DANIES_JWT_TOKEN=; max-age=0";
      subjectIn.disabled = true;
      messageTa.disabled = true;
      oauthBtn.style.display = 'block';
    }
  }
});

'use strict';

// —————————————————————————————————————————————————————————————
// 1) Grabs
const navigationLinks = Array.from(document.querySelectorAll('[data-nav-link]'));
const pages           = Array.from(document.querySelectorAll('[data-page]'));

// —————————————————————————————————————————————————————————————
// 2) Page‑activation helper
function activatePage(pageName) {
  console.log('Activating page:', pageName);
  navigationLinks.forEach((link, idx) => {
    const name = link.innerText.trim().toLowerCase();
    if (name === pageName) {
      pages[idx].classList.add('active');
      link.classList.add('active');
    } else {
      pages[idx].classList.remove('active');
      link.classList.remove('active');
    }
  });
  window.scrollTo(0, 0);
}

// —————————————————————————————————————————————————————————————
// 3) Wire clicks → save & activate
navigationLinks.forEach(link => {
  link.addEventListener('click', () => {
    const pageName = link.innerText.trim().toLowerCase();
    console.log('Clicked page:', pageName);
    sessionStorage.setItem('activePage', pageName);
    activatePage(pageName);
  });
});

// —————————————————————————————————————————————————————————————
// 4) On DOM ready → restore page & scroll
window.addEventListener('DOMContentLoaded', () => {
  // -- restore page
  const savedPage = sessionStorage.getItem('activePage');
  const toShow    = savedPage || 'about';            // default = about
  console.log('Restoring page, saved=', savedPage);
  activatePage(toShow);

  // -- restore scroll
  const savedScroll = sessionStorage.getItem('scrollPos');
  console.log('Restoring scrollPos=', savedScroll);
  if (savedScroll) window.scrollTo(0, parseInt(savedScroll, 10));
});

// —————————————————————————————————————————————————————————————
// 5) Before unload → save scroll
window.addEventListener('beforeunload', () => {
  sessionStorage.setItem('scrollPos', window.scrollY);
  console.log('Saving scrollPos=', window.scrollY);
});




