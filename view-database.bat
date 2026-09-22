@echo off
title Babysitter Booking - Database (PostgreSQL)
echo ===============================================
echo  Babysitter Booking - Database viewer (PostgreSQL)
echo ===============================================
echo.
echo PostgreSQL is running in Docker: babysitter-postgres
echo   Host : localhost:5432
echo   DB   : babysitter_db
echo   User : postgres / postgres
echo.
echo Choose how to view:
echo   [1] Open psql shell (command line)
echo   [2] Show quick stats via psql
echo.
choice /c 12 /n /m "Enter 1 or 2: "
if %errorlevel%==2 goto stats
if %errorlevel%==1 goto shell

:shell
echo.
echo Opening psql shell - type \dt to list tables, SELECT * FROM users; etc.
echo Type \q to exit.
docker exec -it babysitter-postgres psql -U postgres -d babysitter_db
goto end

:stats
echo.
echo --- USERS ---
docker exec babysitter-postgres psql -U postgres -d babysitter_db -c "SELECT id, full_name, email, role, active FROM users ORDER BY id;"
echo.
echo --- BABYSITTERS ---
docker exec babysitter-postgres psql -U postgres -d babysitter_db -c "SELECT b.id, u.full_name AS sitter, b.experience_years, b.hourly_rate, b.verified, b.avg_rating FROM babysitters b JOIN users u ON u.id = b.user_id ORDER BY b.id;"
echo.
echo --- BOOKINGS (with names) ---
docker exec babysitter-postgres psql -U postgres -d babysitter_db -c "SELECT b.id, p.full_name AS parent, s.full_name AS sitter, b.status, b.total_amount FROM bookings b JOIN users p ON p.id = b.parent_id JOIN babysitters bs ON bs.id = b.babysitter_id JOIN users s ON s.id = bs.user_id ORDER BY b.id;"
echo.
echo --- REVIEWS (with names) ---
docker exec babysitter-postgres psql -U postgres -d babysitter_db -c "SELECT r.id, u.full_name AS reviewer, r.rating, r.comment, r.booking_id FROM reviews r JOIN users u ON u.id = r.reviewer_id ORDER BY r.id;"
echo.
echo --- PAYMENTS ---
docker exec babysitter-postgres psql -U postgres -d babysitter_db -c "SELECT p.id, u.full_name AS parent, p.amount, p.method, p.status, p.transaction_ref FROM payments p JOIN bookings b ON b.id = p.booking_id JOIN users u ON u.id = b.parent_id ORDER BY p.id;"
echo.
pause
goto end

:end
pause
