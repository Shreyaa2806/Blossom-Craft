
function addToCart(name, price, image, description) {

    let cart = JSON.parse(localStorage.getItem("cart")) || [];

    let item = cart.find(product => product.name === name);

    if (item) {
        item.quantity++;
        item.image = image;
        item.description = description;
    } else {
        cart.push({
            name: name,
            price: Number(price),
            image: image,
            description: description,
            quantity: 1
        });
    }

    localStorage.setItem("cart", JSON.stringify(cart));

    alert(name + " added to cart successfully! 🛒");
}


function addToWishlist(name, price, image, description) {

    let wishlist = JSON.parse(localStorage.getItem("wishlist")) || [];

    let existing = wishlist.find(item => item.name === name);

    if (existing) {
        alert("Already added to Wishlist! ❤️");
        return;
    }

    wishlist.push({
        name: name,
        price: Number(price),
        image: image,
        description: description
    });

    localStorage.setItem("wishlist", JSON.stringify(wishlist));

    alert(name + " added to Wishlist! ❤️");
}
