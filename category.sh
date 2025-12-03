#!/bin/bash

# === CONFIGURATION ===
LOGIN_URL="http://192.168.29.46:8081/auth/login"
CATEGORIES_URL="http://192.168.29.46:8081/api/categories"
CREDENTIALS_FILE="credentials.txt"

# Admin credentials
ADMIN_USERNAME="admin"
ADMIN_PASSWORD="admin"

# Name arrays
first_names=(
    "Aarav" "Vivaan" "Aditya" "Vihaan" "Arjun" "Sai" "Reyansh" "Aryan" "Krishna" "Ishaan"
    "Shivansh" "Rudra" "Shaurya" "Advait" "Ritvik" "Aadi" "Kabir" "Pranav" "Ayaan" "Dhruv"
    "Rohan" "Anaya" "Diya" "Ira" "Anika" "Myra" "Aadhya" "Anvi" "Saisha" "Kiara"
    "Riya" "Siya" "Prisha" "Aaradhya" "Ananya" "Mira" "Navya" "Aanya" "Pari" "Vanya"
    "Saanvi" "Ishani" "Tanya" "Riddhi" "Mahi" "Kushi" "Avni" "Tanvi" "Shanaya" "Nitya"
)

last_names=(
    "Sharma" "Verma" "Singh" "Kumar" "Mehta" "Gupta" "Jain" "Yadav" "Pandey" "Choudhary"
    "Patel" "Reddy" "Khan" "Mishra" "Agarwal" "Malhotra" "Chatterjee" "Das" "Bose" "Nair"
)

designations=("Bollywood Actor" "Cricketers" "Chessmaster" "User")
designation_emails=("actor" "cricketer" "chess" "user")

total=100
start_id=1
start_contact=9810000001

# === STEP 1: LOGIN AND GET BEARER TOKEN ===
echo "🔐 Logging in to get access token..."

login_response=$(curl -s -X POST "$LOGIN_URL" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$ADMIN_USERNAME\", \"password\":\"$ADMIN_PASSWORD\"}")

# Extract JWT from "data" field (using jq if available, else fallback)
if command -v jq &>/dev/null; then
    token=$(echo "$login_response" | jq -r '.data // empty')
else
    # Fallback: use grep + sed (less robust but works if jq not installed)
    token=$(echo "$login_response" | grep -o '"data":"[^"]*"' | cut -d'"' -f4)
fi

if [ -z "$token" ] || [ "$token" = "null" ]; then
    echo "❌ Failed to extract token. Login response:"
    echo "$login_response"
    exit 1
fi

echo "✅ Login successful. Token acquired (first 30 chars): ${token:0:30}..."

# === STEP 2: PREPARE CREDENTIALS FILE ===
>"$CREDENTIALS_FILE"
echo "username,password" >"$CREDENTIALS_FILE"

# === STEP 3: SEND 800 RECORDS ===
echo "📤 Posting $total category records..."

for ((i = 0; i < total; i++)); do
    id=$((start_id + i))
    contactno=$((start_contact + i))

    first="${first_names[$((i % ${#first_names[@]}))]}"
    last="${last_names[$((i / ${#first_names[@]} % ${#last_names[@]}))]}"
    name="$first $last"

    desig_index=$((i % 4))
    designation="${designations[$desig_index]}"
    desig_email="${designation_emails[$desig_index]}"
    email="${first,,}.${desig_email}@example.com"

    username="${first,,}$id"
    password="vip"

    echo "$username,$password" >>"$CREDENTIALS_FILE"

    json="{\"id\":$id,\"contactno\":\"$contactno\",\"designation\":\"$designation\",\"email\":\"$email\",\"name\":\"$name\",\"status\":\"Inactive\",\"username\":\"$username\",\"password\":\"$password\"}"

    # Send with Bearer token
    curl -s -X POST "$CATEGORIES_URL" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $token" \
        -d "$json" \
        >/dev/null

    if (((i + 1) % 100 == 0)); then
        echo "✅ Sent $((i + 1)) records..."
    fi
done

echo "🎉 All $total records inserted successfully!"
echo "🔑 Credentials saved to: $CREDENTIALS_FILE"
