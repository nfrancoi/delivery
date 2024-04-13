package com.nfrancoi.delivery.repository.retrofit.model;

import java.math.BigDecimal;


public class DeliveryProductJson {

	@Override
	public String toString() {
		return "DeliveryProduct [deliveryProductsId=" + deliveryProductsId + ", deliveryId=" + deliveryId
				+ ", productId=" + productId + ", type=" + type + ", productName=" + productName + ", quantity="
				+ quantity + ", priceUnitVatIncl=" + priceUnitVatIncl + ", vat=" + vat + ", vatApplicable="
				+ vatApplicable + ", priceUnitVatExcl=" + priceUnitVatExcl + ", discount=" + discount
				+ ", priceTotVatExclDiscounted=" + priceTotVatExclDiscounted + ", priceTotVatInclDiscounted="
				+ priceTotVatInclDiscounted + "]";
	}

	public Long deliveryProductsId;

	public Long deliveryId;

	public Long productId;

	public String type; // D:Deposit, T:Take, S:Sell

	public String productName;
	public int quantity;

	public BigDecimal priceUnitVatIncl;
	public BigDecimal vat;

	public BigDecimal vatApplicable;
	public BigDecimal priceUnitVatExcl;
	public BigDecimal discount;
	public BigDecimal priceTotVatExclDiscounted;
	public BigDecimal priceTotVatInclDiscounted;
	



	public DeliveryProductJson() {
		super();
		
	}

	public Long getDeliveryProductsId() {
		return deliveryProductsId;
	}

	public void setDeliveryProductsId(Long deliveryProductsId) {
		this.deliveryProductsId = deliveryProductsId;
	}

	public Long getDeliveryId() {
		return deliveryId;
	}

	public void setDeliveryId(Long deliveryId) {
		this.deliveryId = deliveryId;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getPriceUnitVatIncl() {
		return priceUnitVatIncl;
	}

	public void setPriceUnitVatIncl(BigDecimal priceUnitVatIncl) {
		this.priceUnitVatIncl = priceUnitVatIncl;
	}

	public BigDecimal getVat() {
		return vat;
	}

	public void setVat(BigDecimal vat) {
		this.vat = vat;
	}

	public BigDecimal getVatApplicable() {
		return vatApplicable;
	}

	public void setVatApplicable(BigDecimal vatApplicable) {
		this.vatApplicable = vatApplicable;
	}

	public BigDecimal getPriceUnitVatExcl() {
		return priceUnitVatExcl;
	}

	public void setPriceUnitVatExcl(BigDecimal priceUnitVatExcl) {
		this.priceUnitVatExcl = priceUnitVatExcl;
	}

	public BigDecimal getDiscount() {
		return discount;
	}

	public void setDiscount(BigDecimal discount) {
		this.discount = discount;
	}

	public BigDecimal getPriceTotVatExclDiscounted() {
		return priceTotVatExclDiscounted;
	}

	public void setPriceTotVatExclDiscounted(BigDecimal priceTotVatExclDiscounted) {
		this.priceTotVatExclDiscounted = priceTotVatExclDiscounted;
	}

	public BigDecimal getPriceTotVatInclDiscounted() {
		return priceTotVatInclDiscounted;
	}

	public void setPriceTotVatInclDiscounted(BigDecimal priceTotVatInclDiscounted) {
		this.priceTotVatInclDiscounted = priceTotVatInclDiscounted;
	}

}
