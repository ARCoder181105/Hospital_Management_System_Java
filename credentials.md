## Complete List of Login Credentials

### Administrator Accounts:
**EMP001** / **admin123** - Dr. Sarah Wilson (Administrator)  
**EMP008** / **admin456** - Maria Garcia (IT Administrator)

### Doctor Accounts:
**EMP003** / **doctor123** - Dr. Michael Chen (Senior Doctor - Cardiology)  
**EMP007** / **doctor456** - Dr. Priya Sharma (Doctor - Pediatrics)

### Nursing Staff:
**EMP002** / **nurse123** - Jennifer Martinez (Head Nurse)  
**EMP006** / **nurse456** - David Brown (Nurse - Emergency)

### Support Staff:
**EMP004** / **reception123** - Robert Johnson (Receptionist)  
**EMP005** / **billing123** - Lisa Thompson (Billing Staff)

---

## Quick Copy & Paste Format:

**EMP001** / **admin123**  
**EMP002** / **nurse123**  
**EMP003** / **doctor123**  
**EMP004** / **reception123**  
**EMP005** / **billing123**  
**EMP006** / **nurse456**  
**EMP007** / **doctor456**  
**EMP008** / **admin456**

---

## Role-Based Access Summary:

| Role | Employee Number | Password | Best For Testing |
|------|-----------------|----------|------------------|
| **Administrator** | EMP001 | admin123 | Full system access |
| **Head Nurse** | EMP002 | nurse123 | Patient care & beds |
| **Senior Doctor** | EMP003 | doctor123 | Medical operations |
| **Receptionist** | EMP004 | reception123 | Patient registration |
| **Billing Staff** | EMP005 | billing123 | Billing & discharges |
| **Nurse** | EMP006 | nurse456 | Nursing duties |
| **Doctor** | EMP007 | doctor456 | General medical |
| **IT Admin** | EMP008 | admin456 | Technical access |

---

## SQL Query to View All Credentials:

```sql
SELECT 
    employee_number, 
    password, 
    name, 
    role, 
    department 
FROM employees 
WHERE active = true 
ORDER BY role, employee_number;
```

---

## Recommended Test Logins:

**For Full System Testing:**  
**EMP001** / **admin123**

**For Medical Operations:**  
**EMP003** / **doctor123**

**For Patient Management:**  
**EMP002** / **nurse123**

**For Front Desk Testing:**  
**EMP004** / **reception123**

**For Billing Testing:**  
**EMP005** / **billing123**

All credentials are case-sensitive. Copy and paste exactly as shown above!