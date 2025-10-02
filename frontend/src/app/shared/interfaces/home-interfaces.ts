export interface DataSuccessStory {
  id: number;
  name: string;
  position: string;
  comment: string;
  photo: string;
}

export interface DataPartners {
  name: string;
  logo: string;
  url: string;
}

export interface DataInstructors {
  id: number;
  name: string;
  title: string;
  bio: string;
  photo: string;
  social:{
    linkedin: string;
    github: string;
  }
}

export interface DataFQS{
  id: number;
  question: string;
  answer: string;
}
