import { Component } from '@angular/core';
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'app-error',
  templateUrl: './error.component.html',
  styleUrl: './error.component.css'
})
export class ErrorComponent {
  errorType = "general";

  constructor(private route: ActivatedRoute) {}

  ngOnInit() {
    let type: string | null = null;
    this.route.paramMap.subscribe(params => {
      type = params.get("type");
      if (type==null) {
        this.errorType = "general"
      } else {
        this.errorType = type;
      }
    })

  }

}
