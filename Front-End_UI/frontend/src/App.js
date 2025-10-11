import React, { useEffect, useState } from "react";

function App() {
    const [products, setProducts] = useState([]);
    const [error, setError] = useState(null);

    useEffect(() => {
        // 🔹 هنا كنستعمل API مجانية حقيقية
        fetch("https://fakestoreapi.com/products")
            .then((response) => {
                if (!response.ok) {
                    throw new Error("Erreur réseau: " + response.status);
                }
                return response.json();
            })
            .then((data) => setProducts(data))
            .catch((err) => setError(err.message));
    }, []);

    return (
        <div style={{ padding: "20px", fontFamily: "Arial" }}>
            <h1>🛒 Liste des produi</h1>

            {error && <p style={{ color: "red" }}>Erreur: {error}</p>}
            {products.length === 0 ? (
                <p>Chargement...</p>
            ) : (
                <div
                    style={{
                        display: "grid",
                        gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))",
                        gap: "20px",
                    }}
                >
                    {products.map((product) => (
                        <div
                            key={product.id}
                            style={{
                                border: "1px solid #ddd",
                                borderRadius: "10px",
                                padding: "10px",
                                textAlign: "center",
                                backgroundColor: "#f9f9f9",
                            }}
                        >
                            <img
                                src={product.image}
                                alt={product.title}
                                style={{ width: "100px", height: "100px", objectFit: "contain" }}
                            />
                            <h3 style={{ fontSize: "14px" }}>{product.title}</h3>
                            <p style={{ color: "green" }}>{product.price} $</p>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}

export default App;
