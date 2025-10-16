import { Directive, HostListener } from '@angular/core';
import { ElementRef, Input, Renderer2 } from '@angular/core';

@Directive({
  selector: '[appTiltCard]'
})
export class TiltCardDirective {
  @Input() tiltStrength: number = 15;

  // Inyectamos ElementRef para acceder al elemento DOM, y Renderer2 para manipularlo de forma segura
  constructor(private el: ElementRef, private renderer: Renderer2) {
    // Establecer la propiedad CSS que permite las transformaciones 3D
    this.renderer.setStyle(this.el.nativeElement, 'transform-style', 'preserve-3d');
    
    // MODIFICACIÓN 1: Incluir la transición del 'height' en la base
    // Esto asegura que la directiva respete la animación de altura.
    this.renderer.setStyle(this.el.nativeElement, 'transition', 'transform 0.1s ease-out, height 0.5s ease-in-out');
  }

  /**
   * Captura el evento de movimiento del mouse sobre el elemento host
   * @param event Evento del mouse
   */
  @HostListener('mousemove', ['$event'])
  onMouseMove(event: MouseEvent) {
    const card: HTMLElement = this.el.nativeElement;
    const rect = card.getBoundingClientRect();

    // Calcular la posición del mouse relativa a la tarjeta
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;

    // Normalizar las coordenadas a un rango de -1 (izquierda/arriba) a 1 (derecha/abajo)
    const xNorm = (x / rect.width) * 2 - 1;
    const yNorm = (y / rect.height) * 2 - 1;

    // Calcular los ángulos de rotación.
    const rotateY = xNorm * this.tiltStrength * -1; // Rotación inversa en Y
    const rotateX = yNorm * this.tiltStrength;      // Rotación normal en X

    // Aplicar la transformación CSS
    this.renderer.setStyle(card, 'transform', `
      rotateX(${rotateX}deg)
      rotateY(${rotateY}deg)
    `);
  }

  /**
   * Restablece la rotación cuando el mouse sale del elemento
   */
  @HostListener('mouseleave')
  onMouseLeave() {
    const card: HTMLElement = this.el.nativeElement;

    // MODIFICACIÓN 2: Incluir la transición del 'height' al salir para mantener la suavidad.
    // Usamos el tiempo más lento (0.5s) que ya tienes para la altura.
    this.renderer.setStyle(card, 'transition', 'transform 0.5s ease-out, height 0.5s ease-in-out');

    // Restablecer la rotación
    this.renderer.setStyle(card, 'transform', 'rotateX(0deg) rotateY(0deg)');
  }

  /**
   * Al entrar, ajustamos la transición para que el movimiento de inclinación sea rápido y reactivo
   */
  @HostListener('mouseenter')
  onMouseEnter() {
    const card: HTMLElement = this.el.nativeElement;

    // MODIFICACIÓN 3: Incluir la transición del 'height' al entrar.
    // Usamos el tiempo más rápido (0.1s) para el 'transform' y mantenemos el 0.5s para 'height'.
    this.renderer.setStyle(card, 'transition', 'transform 0.1s ease-out, height 0.5s ease-in-out');
  }
}