export default function CheckoutPage() {
  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">Checkout</h1>
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <div>
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
              Shipping Information
            </h3>
            <p className="text-gray-600">
              Checkout form will be implemented here...
            </p>
            {/* Checkout form will be implemented in later tasks */}
          </div>
        </div>
        <div>
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
              Order Summary
            </h3>
            <p className="text-gray-600">
              Order summary will be displayed here...
            </p>
            {/* PayPal integration will be implemented in later tasks */}
          </div>
        </div>
      </div>
    </div>
  );
}
