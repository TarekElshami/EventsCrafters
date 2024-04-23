import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ReviewService } from '../../services/review.service';
import { UserService } from '../../services/user.service';
import { Router, ActivatedRoute } from '@angular/router';
import { User } from '../../models/user.model';
import { Review } from '../../models/review.model';

@Component({
  selector: 'app-review-form',
  templateUrl: './review-form.component.html',
  styleUrls: ['./review-form.component.css', '../../forms.css']
})
export class ReviewFormComponent implements OnInit {
  reviewForm: FormGroup;
  eventId: number;
  currentUser!: User;
  isLoading = false;  

  constructor(
    private formBuilder: FormBuilder,
    private reviewService: ReviewService,
    private userService: UserService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.eventId = this.route.snapshot.params['id'];
    this.reviewForm = this.formBuilder.group({
      rating: [1, [Validators.required, Validators.min(1), Validators.max(5)]],
      text: ['', [Validators.required, Validators.maxLength(255)]]
    });
  }

  ngOnInit(): void {
    this.isLoading = true;
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentUser = user; 
        this.isLoading = false;  
      },
      error: () => {
        this.router.navigate(['/error']); 
        this.isLoading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.reviewForm.valid && this.currentUser) {
      const reviewData: Review = { 
        rating: this.reviewForm.value.rating,
        text: this.reviewForm.value.text,
        userId: this.currentUser.id,
        eventId: this.eventId
      };
      this.isLoading = true;
      this.reviewService.createReview(reviewData).subscribe({
        next: () => {
          this.router.navigate([`/event/${this.eventId}`]);
          this.isLoading = false;  
        },
        error: () => {
          this.router.navigate(['/error']);
          this.isLoading = false;
        }
      });
    } else {
      console.error('Formulario no válido o usuario no definido');
    }
  }
}
