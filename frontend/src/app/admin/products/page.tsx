export default function AdminProductsPage() {
  return (
    <div>
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Manage Products</h1>
        <button className="btn-primary">Add New Product</button>
      </div>

      <div className="bg-white rounded-lg shadow-sm border border-gray-200">
        <div className="p-6">
          <p className="text-gray-600">
            Product management table will be implemented here...
          </p>
          {/* Product management will be implemented in later tasks */}
        </div>
      </div>
    </div>
  );
}
