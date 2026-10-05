package com.example.huganstorev2.models.product.controller.Dtos;

public record ProductPopularityResponse(
	Long productId,
	long cartQuantity,
	long orderQuantity,
	long totalQuantity,
	int rank) {
}