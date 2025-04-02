import Product from "../interfaces/Product";

export const mockProducts: Product[] = [
    {
        id: 1,
        name: 'Wireless Mouse',
        description: 'A sleek and ergonomic wireless mouse.',
        expirationDate: new Date('2025-12-31'),
        quantity: 50,
        price: 25
    },
    {
        id: 2,
        name: 'Mechanical Keyboard',
        description: 'A durable mechanical keyboard with RGB lighting.',
        expirationDate: new Date('2026-06-15'),
        quantity: 30,
        price: 80
    },
    {
        id: 3,
        name: '4K Monitor',
        description: 'A 27-inch 4K UHD monitor with vibrant colors.',
        expirationDate: new Date('2027-03-10'),
        quantity: 20,
        price: 350
    },
    {
        id: 4,
        name: 'External SSD',
        description: 'A portable SSD with 1TB storage capacity.',
        expirationDate: new Date('2028-01-01'),
        quantity: 40,
        price: 120
    },
    {
        id: 5,
        name: 'Bluetooth Speaker',
        description: 'A compact Bluetooth speaker with excellent sound quality.',
        expirationDate: new Date('2024-09-30'),
        quantity: 60,
        price: 45
    }
];
