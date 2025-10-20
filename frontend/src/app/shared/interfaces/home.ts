export interface SuccessStory {
  id: number,
  name: string,
  position: string,
  comment: string,
  photo: string,
}

export interface Partner {
  name: string,
  logo: string,
  url: string,
}

export interface Instructor {
  id: number,
  name: string,
  title: string,
  bio: string,
  photo: string,
  social:{
    linkedin: string,
    github: string,
  }
}

export interface FQS{
  id: number,
  question: string,
  answer: string,
}

export interface Service{
  id: number,
  name: string,
  comment: string,
  duration: string,
  date: string,
  message: string,
  messageButton: string,
}