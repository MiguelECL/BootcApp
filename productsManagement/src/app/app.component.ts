import { Component, inject, Injectable } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import Product from '../interfaces/Product';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';
import { mockProducts } from '../mock/mockProducts';

@Injectable({providedIn: 'root'})
export class ConfigService {
  constructor(private http: HttpClient) {}

  // returns an observable
  getProducts(){
    this.http.get(environment.getProductsURL, {responseType: 'text'}).subscribe(data =>{
      console.log(data);
    })
  }

}

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {

  // Inject the HttpClient
  private configService = inject(ConfigService);

  title = 'productsManagement';
  items = mockProducts;

  test = (item: Product) => {
    let products$ = this.configService.getProducts();
    return products$;
  }
  
}
