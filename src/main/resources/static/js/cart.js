
function displayCart() {
    let cart = JSON.parse(localStorage.getItem("cart")) || [];
    let container = document.getElementById("cart-items");
    let total = 0;

    container.innerHTML = "";

    if (cart.length === 0) {
        container.innerHTML = "<p>Your cart is empty! 🌸</p>";
    }

    cart.forEach((item, index) => {
        total += item.price * item.quantity;

        container.innerHTML += `
            <div class="cart-item">
                <h3>${item.name}</h3>
                <p>Price: ₹${item.price}</p>
                <button onclick="changeQuantity(${index}, -1)">−</button>
                <span>${item.quantity}</span>
                <button onclick="changeQuantity(${index}, 1)">+</button>
                <button onclick="removeItem(${index})">Remove</button>
            </div>
        `;
    });

    document.getElementById("cart-total").textContent = total;
}

function changeQuantity(index, change) {
    let cart = JSON.parse(localStorage.getItem("cart")) || [];

    cart[index].quantity += change;

    if (cart[index].quantity <= 0) {
        cart.splice(index, 1);
    }

    localStorage.setItem("cart", JSON.stringify(cart));
    displayCart();
}

function removeItem(index) {
    let cart = JSON.parse(localStorage.getItem("cart")) || [];

    cart.splice(index, 1);

    localStorage.setItem("cart", JSON.stringify(cart));
    displayCart();
}

function checkout() {
    let cart = JSON.parse(localStorage.getItem("cart")) || [];

    if (cart.length === 0) {
        alert("Your cart is empty!");
        return;
    }

    window.location.href = "/checkout";
}

displayCart();