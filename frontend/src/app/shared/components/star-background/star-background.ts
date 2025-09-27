import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-star-background',
  imports: [],
  templateUrl: './star-background.html',
  styleUrl: './star-background.css'
})
export class StarBackground implements OnInit {
    ngOnInit(): void {
    const canvas = document.getElementById('starfield') as HTMLCanvasElement | null;

    if (canvas) {
      const c = canvas as HTMLCanvasElement;
      const ctx = c.getContext('2d')!;

      c.width = window.innerWidth;
      c.height = window.innerHeight;

      const STAR_COUNT = 500;
      const stars: Star[] = [];

      function initStars() {
        for (let i = 0; i < STAR_COUNT; i++) {
          stars.push({
            x: Math.random() * c.width,
            y: Math.random() * c.height,
            radius: Math.random() * 1.5,
            velocity: 0.3 + Math.random() * 0.5
          });
        }
      }

      function drawStar(star: Star) {
        ctx.beginPath();
        ctx.arc(star.x, star.y, star.radius, 0, Math.PI * 2);
        ctx.fillStyle = 'white';
        ctx.fill();
      }

      function updateStars() {
        for (const star of stars) {
          star.y -= star.velocity;

          if (star.y < 0) {
            star.y = c.height;
            star.x = Math.random() * c.width;
          }
        }
      }

      function animate() {
        ctx.clearRect(0, 0, c.width, c.height);

        stars.forEach(drawStar);

        updateStars();

        requestAnimationFrame(animate);
      }

      initStars();
      animate();

      window.addEventListener('resize', () => {
        c.width = window.innerWidth;
        c.height = window.innerHeight;
      });
    }
  }

}
