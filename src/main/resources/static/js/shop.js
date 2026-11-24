
function addToCart(name, price) {
    let cart = JSON.parse(localStorage.getItem("cart")) || [];

    let item = cart.find(product => product.name === name);

    if (item) {
        item.quantity++;
    } else {
        cart.push({
            name: name,
            price: price,
            quantity: 1
        });
    }

    localStorage.setItem("cart", JSON.stringify(cart));

    alert(name + " added to cart successfully!");
}

function filterProducts(category) {
    let products = document.querySelectorAll(".product-card");

    products.forEach(product => {
        if (category === "all" ||
            product.dataset.category === category) {
            product.style.display = "block";
        } else {
            product.style.display = "none";
        }
    });
}

function searchProducts() {
    let search = document.getElementById("search").value.toLowerCase();
    let products = document.querySelectorAll(".product-card");

    products.forEach(product => {
        let name = product.querySelector("h3").textContent.toLowerCase();
        product.style.display = name.includes(search) ? "block" : "none";
    });
}
function addToWishlist(name, price) {
    let wishlist = JSON.parse(localStorage.getItem("wishlist")) || [];

    let existing = wishlist.find(item => item.name === name);

    if (existing) {
        alert("Already added to Wishlist! ❤️");
        return;
    }

    wishlist.push({ name: name, price: price });

    localStorage.setItem("wishlist", JSON.stringify(wishlist));

    alert(name + " added to Wishlist! ❤️");
}