#!/bin/bash

# === CONFIGURATION ===
BASE_URL="https://duty-roaster-backend.onrender.com"
LOGIN_URL="$BASE_URL/auth/login"
OFFICER_URL="$BASE_URL/api/officer"
CREDENTIALS_FILE="officer_credentials.csv"

# Admin credentials for login
ADMIN_USERNAME="himanshu"
ADMIN_PASSWORD="admin"

# Name arrays (50 first + 50 last = 2500 unique combinations)
first_names=(
    "Arjun" "Rohan" "Vikas" "Manish" "Avinash" "Sumit" "Karan" "Rahul" "Neeraj" "Yogesh"
    "Sagar" "Mukesh" "Hemant" "Deepak" "Arvind" "Tarun" "Sachin" "Ajay" "Dinesh" "Lalit"
    "Rakesh" "Gaurav" "Pawan" "Suresh" "Kamal" "Chandan" "Dhruv" "Pradeep" "Umesh" "Aditya"
    "Ashok" "Mahesh" "Suraj" "Rajesh" "Vishal" "Nand" "Dev" "Paramjeet" "Ravi" "Sarvesh"
    "Bhavesh" "Dharam" "Sushil" "Tarachand" "Jeet" "Abhinav" "Samar" "Aryan" "Harsh" "Kunal"
)

last_names=(
    "Mehra" "Singh" "Shah" "Patel" "Kumar" "Tomar" "Arora" "Bhatt" "Saxena" "Nair"
    "Malhotra" "Rana" "Verma" "Sharma" "Jain" "Bedi" "Gupta" "Chopra" "Bora" "Mishra"
    "Yadav" "Pillai" "Joshi" "Agarwal" "Sen" "Modi" "Suryavanshi" "Kapoor" "Gera" "Trivedi"
    "Menon" "Raju" "Khanna" "Kulkarni" "Singla" "Kashyap" "Chatterjee" "Sabharwal" "Tiwari" "Bhalla"
    "Pal" "Raichand" "Arvind" "Rajput" "More" "Bhardwaj" "Kohli" "Grover" "Kaul" "Soni"
)

total=800
base_contact=9810001001

# === STEP 1: LOGIN TO GET BEARER TOKEN ===
echo "🔐 Logging in as $ADMIN_USERNAME to get access token..."

login_resp=$(curl -s -X POST "$LOGIN_URL" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$ADMIN_USERNAME\", \"password\":\"$ADMIN_PASSWORD\"}")

# Check if response is HTML (e.g., "Not Found") → means wrong URL
if [[ "$login_resp" == *"Not Found"* ]] || [[ "$login_resp" == *"<html"* ]]; then
    echo "❌ Backend URL returned 'Not Found'. Check if BASE_URL is correct."
    echo "Response: $login_resp"
    exit 1
fi

# Extract token from .data field
if command -v jq &>/dev/null; then
    token=$(echo "$login_resp" | jq -r '.data // empty')
else
    # Fallback: extract value of "data" key using grep/sed
    token=$(echo "$login_resp" | grep -o '"data":"[^"]*"' | cut -d'"' -f4)
fi

if [ -z "$token" ] || [ "$token" = "null" ]; then
    echo "❌ Failed to extract token. Login response:"
    echo "$login_resp"
    exit 1
fi

echo "✅ Login successful. Proceeding to create officers..."

# === STEP 2: INITIALIZE CREDENTIALS FILE ===
>"$CREDENTIALS_FILE"
echo "username,password" >"$CREDENTIALS_FILE"

# === STEP 3: CREATE 800 OFFICERS ===
echo "📤 Seeding $total officers with hierarchical ranks..."

for ((i = 1; i <= total; i++)); do

    # ===== RANK LOGIC: Senior (A) = few, Junior (E) = many =====
    if ((i <= 50)); then
        rank="A Grade"
    elif ((i <= 150)); then # 100 officers
        rank="B Grade"
    elif ((i <= 300)); then # 150 officers
        rank="C Grade"
    elif ((i <= 500)); then # 200 officers
        rank="D Grade"
    else # 300 officers (501–800)
        rank="E Grade"
    fi

    # ===== UNIQUE NAME (no numbers appended) =====
    first="${first_names[$(((i - 1) % ${#first_names[@]}))]}"
    last="${last_names[$(((i - 1) / ${#first_names[@]} % ${#last_names[@]}))]}"
    name="$first $last"

    # ===== USERNAME & EMAIL =====
    username="${first,,}${i}"
    email="${first,,}.${last,,}.${i}@example.com"

    # ===== OTHER FIELDS =====
    contactno=$((base_contact + i - 1))
    experience=$(((i - 1) % 20 + 1)) # 1 to 20 years
    password="Officer@$i"
    status="Inactive"

    # ===== SAVE CREDENTIALS =====
    echo "$username,$password" >>"$CREDENTIALS_FILE"

    # ===== JSON PAYLOAD (NO 'id' — let DB generate) =====
    json="{\"contactno\":\"$contactno\",\"email\":\"$email\",\"experience\":$experience,\"name\":\"$name\",\"rank\":\"$rank\",\"status\":\"$status\",\"username\":\"$username\",\"password\":\"$password\"}"

    # ===== POST WITH AUTHORIZATION =====
    curl -s -X POST "$OFFICER_URL" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $token" \
        -d "$json" \
        >/dev/null

    # Progress indicator
    if ((i % 100 == 0)); then
        echo "✅ $i officers created..."
    fi
done

echo "🎉 All $total officers seeded successfully!"
echo "🔑 Credentials saved to: $CREDENTIALS_FILE"
