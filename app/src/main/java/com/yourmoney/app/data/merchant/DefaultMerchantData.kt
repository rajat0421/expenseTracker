package com.yourmoney.app.data.merchant

/** One seeded merchant and the narration fragments ("aliases") that identify it. [categoryKey]
 * must match a [com.yourmoney.app.data.local.DefaultCategories] key. */
data class SeedMerchant(val displayName: String, val categoryKey: String, val aliases: List<String>)

/**
 * Curated Indian merchant/alias database (~130 entries) so SMS-detected and manually-typed
 * expenses get a real business name and category instead of generic keyword guesses.
 *
 * Deliberately excludes a few short/ambiguous aliases a naive list would include (bare "VI",
 * "PW", "ALLEN") since even whole-word matching can't fully rule out false positives for a
 * 2-3 letter token - see the Phase notes for the full list of what was left out and why.
 */
object DefaultMerchantData {

    fun seed(): List<SeedMerchant> = listOf(
        // Food & Dining
        SeedMerchant("Swiggy", "FOOD", listOf("SWIGGY", "SWIGGY LTD", "SWIGGY TECHNOLOGIES")),
        SeedMerchant("Zomato", "FOOD", listOf("ZOMATO", "ZOMATO LTD", "ZOMATO MEDIA")),
        SeedMerchant("Domino's", "FOOD", listOf("DOMINOS", "DOMINOS PIZZA", "JUBILANT FOODWORKS")),
        SeedMerchant("McDonald's", "FOOD", listOf("MCDONALDS", "MCDONALD'S")),
        SeedMerchant("KFC", "FOOD", listOf("KFC", "KENTUCKY FRIED CHICKEN")),
        SeedMerchant("Pizza Hut", "FOOD", listOf("PIZZA HUT", "PIZZAHUT")),
        SeedMerchant("Burger King", "FOOD", listOf("BURGER KING", "BK INDIA")),
        SeedMerchant("Subway", "FOOD", listOf("SUBWAY")),
        SeedMerchant("Starbucks", "FOOD", listOf("STARBUCKS", "TATA STARBUCKS")),
        SeedMerchant("EatFit", "FOOD", listOf("EATFIT", "CUREFOOD")),
        SeedMerchant("Faasos", "FOOD", listOf("FAASOS", "REBEL FOODS")),
        SeedMerchant("Behrouz Biryani", "FOOD", listOf("BEHROUZ", "BEHROUZ BIRYANI")),
        SeedMerchant("Oven Story", "FOOD", listOf("OVEN STORY")),
        SeedMerchant("Wow! Momo", "FOOD", listOf("WOW MOMO")),
        SeedMerchant("Chaayos", "FOOD", listOf("CHAAYOS")),
        SeedMerchant("Blue Tokai", "FOOD", listOf("BLUE TOKAI")),
        SeedMerchant("Cafe Coffee Day", "FOOD", listOf("CCD", "CAFE COFFEE DAY")),
        SeedMerchant("Third Wave Coffee", "FOOD", listOf("THIRD WAVE COFFEE", "THIRD WAVE")),

        // Groceries
        SeedMerchant("Blinkit", "GROCERIES", listOf("BLINKIT", "GROFERS")),
        SeedMerchant("Zepto", "GROCERIES", listOf("ZEPTO")),
        SeedMerchant("BigBasket", "GROCERIES", listOf("BIGBASKET", "BIG BASKET", "BBNOW", "BB NOW")),
        SeedMerchant("Swiggy Instamart", "GROCERIES", listOf("SWIGGY INSTAMART", "INSTAMART")),
        SeedMerchant("DMart", "GROCERIES", listOf("DMART", "AVENUE SUPERMARTS")),
        SeedMerchant("JioMart", "GROCERIES", listOf("JIOMART", "JIO MART")),
        SeedMerchant("Reliance Smart", "GROCERIES", listOf("RELIANCE SMART", "SMART BAZAAR")),
        SeedMerchant("More", "GROCERIES", listOf("MORE RETAIL", "MORE SUPERMARKET")),
        SeedMerchant("Spencer's", "GROCERIES", listOf("SPENCERS", "SPENCER'S")),
        SeedMerchant("Nature's Basket", "GROCERIES", listOf("NATURES BASKET", "NATURE'S BASKET")),
        SeedMerchant("Star Bazaar", "GROCERIES", listOf("STAR BAZAAR")),
        SeedMerchant("Ratnadeep", "GROCERIES", listOf("RATNADEEP")),

        // Transportation
        SeedMerchant("Uber", "TRANSPORT", listOf("UBER", "UBER INDIA", "UBER TRIP")),
        SeedMerchant("Ola", "TRANSPORT", listOf("OLA", "OLACABS", "ANI TECHNOLOGIES")),
        SeedMerchant("Rapido", "TRANSPORT", listOf("RAPIDO", "RAPIDO BIKE TAXI")),
        SeedMerchant("Namma Yatri", "TRANSPORT", listOf("NAMMA YATRI", "JUSPAY")),
        SeedMerchant("BluSmart", "TRANSPORT", listOf("BLUSMART", "BLU SMART")),
        SeedMerchant("RedBus", "TRANSPORT", listOf("REDBUS", "RED BUS")),
        SeedMerchant("IRCTC", "TRANSPORT", listOf("IRCTC", "INDIAN RAILWAYS")),
        SeedMerchant("Metro", "TRANSPORT", listOf("BMRCL", "NAMMA METRO", "DMRC", "CMRL")),
        SeedMerchant("FASTag", "TRANSPORT", listOf("FASTAG", "NHAI FASTAG", "ICICI FASTAG")),
        SeedMerchant("HP Petrol", "TRANSPORT", listOf("HPCL", "HINDUSTAN PETROLEUM")),
        SeedMerchant("Indian Oil", "TRANSPORT", listOf("IOCL", "INDIAN OIL")),
        SeedMerchant("BPCL", "TRANSPORT", listOf("BPCL", "BHARAT PETROLEUM")),
        SeedMerchant("Shell", "TRANSPORT", listOf("SHELL INDIA", "SHELL PETROL")),
        SeedMerchant("AbhiBus", "TRANSPORT", listOf("ABHIBUS")),
        SeedMerchant("KSRTC", "TRANSPORT", listOf("KSRTC")),

        // Shopping
        SeedMerchant("Amazon", "SHOPPING", listOf("AMAZON", "AMAZON INDIA", "AMAZON SELLER")),
        SeedMerchant("Flipkart", "SHOPPING", listOf("FLIPKART", "FLIPKART INTERNET")),
        SeedMerchant("Myntra", "SHOPPING", listOf("MYNTRA")),
        SeedMerchant("Ajio", "SHOPPING", listOf("AJIO", "RELIANCE AJIO")),
        SeedMerchant("Meesho", "SHOPPING", listOf("MEESHO")),
        SeedMerchant("Nykaa", "SHOPPING", listOf("NYKAA", "FSN E COMMERCE")),
        SeedMerchant("Croma", "SHOPPING", listOf("CROMA", "INFINITI RETAIL")),
        SeedMerchant("Reliance Digital", "SHOPPING", listOf("RELIANCE DIGITAL")),
        SeedMerchant("Vijay Sales", "SHOPPING", listOf("VIJAY SALES")),
        SeedMerchant("IKEA", "SHOPPING", listOf("IKEA", "IKEA INDIA")),
        SeedMerchant("Decathlon", "SHOPPING", listOf("DECATHLON")),
        SeedMerchant("FirstCry", "SHOPPING", listOf("FIRSTCRY", "BRAINBEES")),
        SeedMerchant("Shoppers Stop", "SHOPPING", listOf("SHOPPERS STOP")),
        SeedMerchant("Lifestyle", "SHOPPING", listOf("LIFESTYLE STORES")),
        SeedMerchant("Westside", "SHOPPING", listOf("WESTSIDE", "TRENT")),
        SeedMerchant("Zara", "SHOPPING", listOf("ZARA")),
        SeedMerchant("H&M", "SHOPPING", listOf("H&M", "H AND M")),
        SeedMerchant("TATA CLiQ", "SHOPPING", listOf("TATA CLIQ", "TATACLIQ")),
        SeedMerchant("Lenskart", "SHOPPING", listOf("LENSKART")),
        SeedMerchant("Titan", "SHOPPING", listOf("TITAN", "TITAN COMPANY")),
        SeedMerchant("Tanishq", "SHOPPING", listOf("TANISHQ")),
        SeedMerchant("CaratLane", "SHOPPING", listOf("CARATLANE")),
        SeedMerchant("Reliance Trends", "SHOPPING", listOf("RELIANCE TRENDS")),

        // Entertainment & Subscriptions
        SeedMerchant("Netflix", "ENTERTAINMENT", listOf("NETFLIX")),
        SeedMerchant("Spotify", "ENTERTAINMENT", listOf("SPOTIFY")),
        SeedMerchant("YouTube Premium", "ENTERTAINMENT", listOf("YOUTUBE PREMIUM", "GOOGLE YOUTUBE")),
        SeedMerchant("Amazon Prime", "ENTERTAINMENT", listOf("AMAZON PRIME", "PRIME VIDEO")),
        SeedMerchant("JioHotstar", "ENTERTAINMENT", listOf("JIOHOTSTAR", "HOTSTAR", "DISNEY HOTSTAR")),
        SeedMerchant("Sony LIV", "ENTERTAINMENT", listOf("SONYLIV", "SONY LIV")),
        SeedMerchant("Zee5", "ENTERTAINMENT", listOf("ZEE5")),
        SeedMerchant("JioSaavn", "ENTERTAINMENT", listOf("JIOSAAVN", "JIO SAAVN")),
        SeedMerchant("Gaana", "ENTERTAINMENT", listOf("GAANA")),
        SeedMerchant("BookMyShow", "ENTERTAINMENT", listOf("BOOKMYSHOW", "BMS")),
        SeedMerchant("PVR INOX", "ENTERTAINMENT", listOf("PVR INOX", "PVR", "INOX")),
        SeedMerchant("Steam", "ENTERTAINMENT", listOf("STEAM GAMES", "VALVE")),
        SeedMerchant("PlayStation", "ENTERTAINMENT", listOf("PLAYSTATION", "SONY INTERACTIVE")),
        SeedMerchant("Google Play", "ENTERTAINMENT", listOf("GOOGLE PLAY")),
        SeedMerchant("Apple Services", "ENTERTAINMENT", listOf("ITUNES", "APPLE COM BILL")),

        // Bills & Utilities
        SeedMerchant("Jio", "BILLS", listOf("RELIANCE JIO", "JIO PREPAID", "JIO POSTPAID", "JIO")),
        SeedMerchant("Airtel", "BILLS", listOf("AIRTEL", "BHARTI AIRTEL")),
        SeedMerchant("Vi (Vodafone Idea)", "BILLS", listOf("VODAFONE IDEA")),
        SeedMerchant("BSNL", "BILLS", listOf("BSNL", "BHARAT SANCHAR NIGAM")),
        SeedMerchant("Tata Play", "BILLS", listOf("TATA PLAY", "TATASKY")),
        SeedMerchant("JioFiber", "BILLS", listOf("JIOFIBER", "JIO FIBRE")),
        SeedMerchant("BESCOM", "BILLS", listOf("BESCOM", "BANGALORE ELECTRICITY")),
        SeedMerchant("Adani Electricity", "BILLS", listOf("ADANI ELECTRICITY")),
        SeedMerchant("Tata Power", "BILLS", listOf("TATA POWER")),
        SeedMerchant("MSEDCL", "BILLS", listOf("MSEDCL", "MAHAVITARAN")),
        SeedMerchant("CESC", "BILLS", listOf("CESC")),
        SeedMerchant("BWSSB", "BILLS", listOf("BWSSB", "BANGALORE WATER SUPPLY")),
        SeedMerchant("Indane", "BILLS", listOf("INDANE GAS", "INDIAN OIL LPG")),
        SeedMerchant("HP Gas", "BILLS", listOf("HP GAS", "HPCL LPG")),
        SeedMerchant("Bharat Gas", "BILLS", listOf("BHARAT GAS", "BPCL LPG")),
        SeedMerchant("LIC", "BILLS", listOf("LIC", "LIFE INSURANCE CORPORATION")),
        SeedMerchant("HDFC Life", "BILLS", listOf("HDFC LIFE")),
        SeedMerchant("ICICI Prudential", "BILLS", listOf("ICICI PRUDENTIAL", "ICICI PRU")),
        SeedMerchant("Max Life", "BILLS", listOf("MAX LIFE")),

        // Healthcare
        SeedMerchant("Tata 1mg", "HEALTH", listOf("TATA 1MG", "1MG")),
        SeedMerchant("PharmEasy", "HEALTH", listOf("PHARMEASY")),
        SeedMerchant("Apollo Pharmacy", "HEALTH", listOf("APOLLO PHARMACY", "APOLLO 24 7")),
        SeedMerchant("Netmeds", "HEALTH", listOf("NETMEDS")),
        SeedMerchant("MedPlus", "HEALTH", listOf("MEDPLUS")),
        SeedMerchant("Practo", "HEALTH", listOf("PRACTO")),
        SeedMerchant("Dr Lal PathLabs", "HEALTH", listOf("LAL PATHLABS", "DR LAL")),
        SeedMerchant("Metropolis", "HEALTH", listOf("METROPOLIS")),
        SeedMerchant("Thyrocare", "HEALTH", listOf("THYROCARE")),
        SeedMerchant("Healthians", "HEALTH", listOf("HEALTHIANS")),

        // Education
        SeedMerchant("Physics Wallah", "EDUCATION", listOf("PHYSICSWALLAH")),
        SeedMerchant("Unacademy", "EDUCATION", listOf("UNACADEMY")),
        SeedMerchant("Coursera", "EDUCATION", listOf("COURSERA")),
        SeedMerchant("Udemy", "EDUCATION", listOf("UDEMY")),
        SeedMerchant("upGrad", "EDUCATION", listOf("UPGRAD")),
        SeedMerchant("Scaler", "EDUCATION", listOf("SCALER")),
        SeedMerchant("Simplilearn", "EDUCATION", listOf("SIMPLILEARN")),
        SeedMerchant("BYJU'S", "EDUCATION", listOf("BYJUS", "BYJU'S")),
        SeedMerchant("Allen Career Institute", "EDUCATION", listOf("ALLEN CAREER INSTITUTE")),
        SeedMerchant("Vedantu", "EDUCATION", listOf("VEDANTU")),

        // Travel & Accommodation
        SeedMerchant("MakeMyTrip", "TRAVEL", listOf("MAKEMYTRIP", "MMT")),
        SeedMerchant("Goibibo", "TRAVEL", listOf("GOIBIBO")),
        SeedMerchant("Cleartrip", "TRAVEL", listOf("CLEARTRIP")),
        SeedMerchant("EaseMyTrip", "TRAVEL", listOf("EASEMYTRIP", "EMT")),
        SeedMerchant("Yatra", "TRAVEL", listOf("YATRA")),
        SeedMerchant("OYO", "TRAVEL", listOf("OYO", "OYO ROOMS")),
        SeedMerchant("Airbnb", "TRAVEL", listOf("AIRBNB")),
        SeedMerchant("Booking.com", "TRAVEL", listOf("BOOKING COM")),

        // Personal care & home services -> no dedicated category, map to Other
        SeedMerchant("Urban Company", "OTHER", listOf("URBAN COMPANY", "URBANCLAP")),
        SeedMerchant("Lakme", "OTHER", listOf("LAKME SALON", "LAKME")),
        SeedMerchant("Cult.fit", "OTHER", listOf("CULTFIT", "CULT FIT")),
        SeedMerchant("Looks Salon", "OTHER", listOf("LOOKS SALON")),
        SeedMerchant("NoBroker", "OTHER", listOf("NOBROKER")),
        SeedMerchant("Porter", "OTHER", listOf("PORTER")),

        // Government & fees -> no dedicated category, map to Other
        SeedMerchant("BBPS", "OTHER", listOf("BBPS")),
        SeedMerchant("Passport Seva", "OTHER", listOf("PASSPORT SEVA")),
        SeedMerchant("Karnataka One", "OTHER", listOf("KARNATAKA ONE")),
        SeedMerchant("Traffic Challan", "OTHER", listOf("TRAFFIC CHALLAN", "ECHALLAN")),
    )
}
