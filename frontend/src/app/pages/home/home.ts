import { Component, signal } from '@angular/core';
import { NavBar } from '../../shared/components/nav-bar/nav-bar';
import { StarBackground } from '../../shared/components/star-background/star-background';
import { CommonModule } from '@angular/common';
import { DiplomadoMasterclass } from "./diplomado-masterclass/diplomado-masterclass";
import { InitialInformation } from "./initial-information/initial-information";
import { Instructors } from "./instructors/instructors";
import { Fqs } from "./fqs/fqs";
import { Partners } from "./partners/partners";
import { Video } from "./video/video";

@Component({
  selector: 'app-home',
  imports: [NavBar, StarBackground, CommonModule, DiplomadoMasterclass, InitialInformation, Instructors, Fqs, Partners, Video],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home {
  cronograma = signal([
    { id: 1, producto: 'Curso de Fundamentos de IA', descripcion: 'Explora los conceptos básicos y aplicaciones de la IA.', fechaInicio: '10 de septiembre, 2024', fechaFin: '15 de noviembre, 2024' },
    { id: 2, producto: 'Especialización en Machine Learning', descripcion: 'Profundiza en algoritmos y modelos de aprendizaje automático.', fechaInicio: '20 de septiembre, 2024', fechaFin: '20 de enero, 2025' },
    { id: 3, producto: 'Certificación en Deep Learning Avanzado', descripcion: 'Crea redes neuronales complejas para problemas reales.', fechaInicio: '05 de octubre, 2024', fechaFin: '05 de marzo, 2025' },
  ]);

  instructores = signal([
    { id: 1, nombre: 'Dr. Alejandro Vargas', titulo: 'Especialista en IA', bio: 'Doctor en Ciencias de la Computación con más de 10 años de experiencia.', foto: 'https://placehold.co/128x128/9CA3AF/FFFFFF?text=AV', redes: {linkedin : 'https://www.linkedin.com/', github: 'https://github.com/'} },
    { id: 2, nombre: 'Ing. Laura Gómez', titulo: 'Ingeniera de Datos', bio: 'Experta en Big Data y desarrollo de soluciones basadas en datos.', foto: 'https://placehold.co/128x128/9CA3AF/FFFFFF?text=LG', redes: {linkedin : 'https://www.linkedin.com/', github: 'https://github.com/'}},
    { id: 3, nombre: 'MSc. Carlos Ríos', titulo: 'Investigador en Visión Artificial', bio: 'Maestro en IA con especialidad en procesamiento de imágenes.', foto: 'https://placehold.co/128x128/9CA3AF/FFFFFF?text=CR', redes: {linkedin : 'https://www.linkedin.com/', github: 'https://github.com/'}},
    { id: 4, nombre: 'MSc. Carlos Ríos', titulo: 'Investigador en Visión Artificial', bio: 'Maestro en IA con especialidad en procesamiento de imágenes.', foto: 'https://placehold.co/128x128/9CA3AF/FFFFFF?text=CR', redes: {linkedin : 'https://www.linkedin.com/', github: 'https://github.com/'}},
  ]);

  faqs = signal([
    { id: 1, pregunta: '¿Cuáles son los requisitos para unirse a los cursos?', respuesta: 'Nuestros cursos están diseñados para todos los niveles. Los cursos básicos no tienen requisitos previos. Para los avanzados, se recomienda tener conocimientos de programación y matemáticas.' },
    { id: 2, pregunta: '¿Los cursos son presenciales o en línea?', respuesta: 'Todos nuestros cursos se imparten en línea para que puedas aprender desde cualquier lugar y a tu propio ritmo.' },
    { id: 3, pregunta: '¿Se emiten certificados al finalizar el curso?', respuesta: 'Sí, al completar satisfactoriamente cada curso, recibirás un certificado oficial de AcademIA.' },
    { id: 4, pregunta: '¿Tienen soporte técnico o académico?', respuesta: 'Sí, ofrecemos soporte 24/7 a través de nuestra plataforma y sesiones en vivo con instructores.' },
  ]);

  testimonios = signal([
    { id: 1, nombre: 'Sofía Martínez', cargo: 'Analista de Datos', comentario: 'AcademIA me dio las herramientas para transformar mi carrera. ¡El curso de Machine Learning es excepcional!' , foto: 'https://placehold.co/80x80/9CA3AF/FFFFFF?text=SM'},
    { id: 2, nombre: 'Juan Pérez', cargo: 'Desarrollador de Software', comentario: 'La calidad de los instructores y el contenido es inigualable. Pude aplicar lo aprendido a mi trabajo de inmediato.', foto: 'https://placehold.co/80x80/9CA3AF/FFFFFF?text=JP'},
    { id: 3, nombre: 'Ana López', cargo: 'Estudiante', comentario: 'Gracias a AcademIA, ahora tengo una base sólida para mi futuro en el mundo de la inteligencia artificial. ¡Totalmente recomendado!', foto: 'https://placehold.co/80x80/9CA3AF/FFFFFF?text=AL'},
    { id: 4, nombre: 'Sofía Martínez', cargo: 'Analista de Datos', comentario: 'AcademIA me dio las herramientas para transformar mi carrera. ¡El curso de Machine Learning es excepcional!' , foto: 'https://placehold.co/80x80/9CA3AF/FFFFFF?text=SM'},
    { id: 5, nombre: 'Juan Pérez', cargo: 'Desarrollador de Software', comentario: 'La calidad de los instructores y el contenido es inigualable. Pude aplicar lo aprendido a mi trabajo de inmediato.', foto: 'https://placehold.co/80x80/9CA3AF/FFFFFF?text=JP'},
    { id: 6, nombre: 'Ana López', cargo: 'Estudiante', comentario: 'Gracias a AcademIA, ahora tengo una base sólida para mi futuro en el mundo de la inteligencia artificial. ¡Totalmente recomendado!', foto: 'https://placehold.co/80x80/9CA3AF/FFFFFF?text=AL'},
  ]);

  colaboradores = signal([
    { nombre: 'RadonSoft', logo: '' },
    { nombre: 'Microsoft', logo: 'https://cdn-icons-png.flaticon.com/512/732/732249.png' },
    { nombre: 'IBM', logo: 'https://cdn-icons-png.flaticon.com/512/732/732228.png' },
    { nombre: 'NVIDIA', logo: 'https://cdn-icons-png.flaticon.com/512/732/732252.png' },
    { nombre: 'Amazon Web Services', logo: 'https://cdn-icons-png.flaticon.com/512/5968/5968369.png' },
  ]);


  currentIndex = signal(0);
  intervalId: any;
  itemsPerSlide = 3;

  constructor() { }

  ngOnInit(): void {
    this.startAutoSlide();
  }

  ngAfterViewInit(): void {
    this.setupManualNavigation();
  }

  ngOnDestroy(): void {
    clearInterval(this.intervalId);
  }

  // --- Lógica del Slider ---

  startAutoSlide(): void {
    this.intervalId = setInterval(() => {
      this.nextSlide();
    }, 5000); // Cambia cada 5 segundos (5000ms)
  }

  setupManualNavigation(): void {
    const prevBtn = document.querySelector('.prev-btn');
    const nextBtn = document.querySelector('.next-btn');

    if (prevBtn) {
      prevBtn.addEventListener('click', () => {
        this.prevSlide();
      });
    }

    if (nextBtn) {
      nextBtn.addEventListener('click', () => {
        this.nextSlide();
      });
    }
  }

  nextSlide(): void {
    const totalItems = this.testimonios().length;
    let nextIndex = this.currentIndex() + this.itemsPerSlide;

    if (nextIndex >= totalItems) {
      nextIndex = 0; // Vuelve al inicio
    }

    this.goToSlide(nextIndex);
  }

  prevSlide(): void {
    const totalItems = this.testimonios().length;
    let prevIndex = this.currentIndex() - this.itemsPerSlide;

    if (prevIndex < 0) {
      prevIndex = totalItems - this.itemsPerSlide; // Vuelve al final
    }

    this.goToSlide(prevIndex);
  }

  goToSlide(index: number): void {
    const wrapper = document.querySelector('.slider-wrapper');
    if (wrapper) {
        (wrapper as HTMLElement).style.transform = `translateX(-${index * (100 / this.itemsPerSlide)}%)`;
      this.currentIndex.set(index);
    }
  }


}
