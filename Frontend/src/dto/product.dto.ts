export interface ProductDTO {
  id: number;
  name: string;
  description?: string;
  price: number;
  category: string;
  capacity: number;
  imageUrl?: string;
  status?: string;
}
