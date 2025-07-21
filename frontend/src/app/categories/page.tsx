export default function CategoriesPage() {
  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">
        Product Categories
      </h1>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {/* Category grid will be implemented in later tasks */}
        {["Saddles", "Bridles", "Boots", "Apparel"].map((category) => (
          <div
            key={category}
            className="bg-white rounded-lg shadow-sm border border-gray-200 p-6"
          >
            <h3 className="text-lg font-semibold text-gray-900 mb-2">
              {category}
            </h3>
            <p className="text-gray-600">Browse {category.toLowerCase()}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
