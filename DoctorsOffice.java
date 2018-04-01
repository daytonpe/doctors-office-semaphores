/*
Patrick Dayton
Project 2 -- Doctor's Office Simulator with Semaphores
CS 5348 -- Prof Ozbirn
Due: 31 March 2018
 */

package doctorsoffice;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class DoctorsOffice{

    /*Shared data members*/
    int TIMER = 1000; // milliseconds of pause between steps
    int NUMPATIENTS; // number of patients in this simulation
    int NUMDOCTORS; // number of doctors in this simulation
    int NUMNURSES; // number of nurses in this simulation
    List<Patient> enteredList = new ArrayList<>(); //pre receptionist list for patients
    List<Patient> waitroomList = new ArrayList<>();//post receptionist, pre nurse list for patients
    List<Patient> exitedList   = new ArrayList<>(); // post doctor visit list for patients
    Receptionist RECEPTIONIST; //Single instance of the receptionist thread

    /*Shared semaphores*/
    static Semaphore consumeReceptionistSem = new Semaphore(1); //start with one receptionist that is not busy
    static Semaphore produceReceptionistSem = new Semaphore(0); //consume/produce toggle when the receptionist is busy
    static Semaphore waitingRoomSem = new Semaphore(0); //waiting room semaphore
    static Semaphore enteredSem = new Semaphore(0); //entered building semaphore
    static Semaphore nurseSem = new Semaphore(1); // nurse is busy semaphore

    // Doctors Office Constructor
    DoctorsOffice(int p, int d){
        NUMPATIENTS = p;
        NUMDOCTORS = d;
        RECEPTIONIST = new Receptionist();

        // Create Doctor and Nurse Threads
        for (int i = 1; i <= d; i++) {
            new Doctor(i);
        }

        // Create Patient Threads
        for (int i = 1; i <= p; i++) {
            try{
                Thread.sleep(TIMER);
                new Patient(i);
            }catch(InterruptedException e) {
                System.out.println("InterruptedException caught");
            }

        }

    }

    /**************************************************************************/
    /**************************************************************************/

    // Patient class
    class Patient implements Runnable{

        int PatientID;
        int doctorSeen; //each patient sees only one doctor. Data member for ease of printing
        boolean seenReceptionist; //move onto next portion of the run() method after seeing receptionist
        boolean exited; //denote that this patient has exited
        Semaphore seenReceptionistSem = new Semaphore(0);
        Semaphore seenDoctorSem = new Semaphore(0);

        /*Patient Constructor*/
        Patient(int i){
            PatientID = i;
            seenReceptionist = false;
            exited = false;
            Thread t = new Thread(this, "Doctor");
            t.start(); // not a daemon thread, so program won't die when the first one exits.
        }

        @Override
        public void run() {

            System.out.println("Patient "+PatientID+" enters waiting room, waits for receptionist");
            enteredList.add(this); // add to entered but not checked in list for the Receptionist to grab from

            enteredSem.release(); // increment the number of entered but not checked in Patients
            while (!seenReceptionist){
                try{
                    // acquire the receptionist to be checked in
                    consumeReceptionistSem.acquire();

                    // note that we HAVE seen the receptionist
                    seenReceptionist = true;

                } catch(InterruptedException e) {
                    System.out.println("InterruptedException caught");
                }
            }

            // Release Receptionist to check in someone else.
            produceReceptionistSem.release();
            waitroomList.add(this);

            waitingRoomSem.release();


            while (!exited){
                try{
                    seenDoctorSem.acquire();
                    // wait then print the prompt
                    Thread.sleep(TIMER);
                    System.out.println("Patient "+PatientID+" receives advice from doctor "+doctorSeen);

                    // allow the patient to leave
                    Thread.sleep(TIMER);
                    System.out.println("Patient "+PatientID+" leaves");
                    exited = true;
                    exitedList.add(this);

                } catch(InterruptedException e) {
                    System.out.println("InterruptedException caught");
                }
            }
        }
    }

    /**************************************************************************/
    /**************************************************************************/

    // Doctor class
    class Doctor implements Runnable{
        int DoctorID;
        Nurse nurse;
        int i;

        // daemon thread because it should die when patients are done funneling through
        Doctor(int i){
            this.DoctorID = i;
            nurse = new Nurse(DoctorID);
            Thread t = new Thread(this, "Doctor");
            t.setDaemon(true);
            t.start();
        }

        @Override
        public void run(){
            while(true){
                try{

                    nurse.consumeExamRoom.acquire();
//                    System.out.println("DOCTOR SEES PATIENT "+nurse.currentPatient.PatientID);
                    // wait then print the prompt
                    Thread.sleep(TIMER);
                    System.out.println("Doctor "+DoctorID+" listens to symptoms from patient "+nurse.currentPatient.PatientID);

                    // let patient know doctor number and that that s/he should be listening to advice from doctor
                    nurse.currentPatient.doctorSeen = DoctorID;
                    nurse.currentPatient.seenDoctorSem.release();

                    // set nurse to no longer busy
                    nurse.nurseSem.release();

                    // patient leaves exam room, release for nurse to fill again
                    nurse.produceExamRoom.release();

                } catch(InterruptedException e) {
                    System.out.println("InterruptedException caught");
                }
            }
        }
    }

    /**************************************************************************/
    /**************************************************************************/

    // Nurse class
    class Nurse implements Runnable{
        int NurseID;

        /*Semaphore to note that nurse is/isnt busy*/
        Semaphore nurseSem = new Semaphore(1);
        Semaphore produceExamRoom = new Semaphore(1);
        Semaphore consumeExamRoom = new Semaphore(0);

        Patient currentPatient;

        Nurse(int i) {
            this.NurseID = i;
            Thread t = new Thread(this, "Nurse");
            t.setDaemon(true);
            t.start();
        }

        @Override
        public void run() {
            while(true){
                try{
                    // double semaphore to make sure nurse places patient in waitroom before doctor treats
                    produceExamRoom.acquire();
                    // grab somone from the waitroom
                    waitingRoomSem.acquire();
                    currentPatient = waitroomList.get(0);

                    try{
                        currentPatient.seenReceptionistSem.acquire();
                        currentPatient = waitroomList.remove(0);
                    }catch(InterruptedException e) {
                        System.out.println("InterruptedException caught");
                    }

                    // set myself to busy
                    nurseSem.acquire();

                    // wait then print the prompt
                    Thread.sleep(TIMER);
                    System.out.println("Nurse "+NurseID+" takes patient "+currentPatient.PatientID+" to doctor's office");

                    // okay doctor, you can now see the patient
                    consumeExamRoom.release();

                } catch(InterruptedException e) {
                    System.out.println("InterruptedException caught");
                }
            }
        }
    }

    /**************************************************************************/
    /**************************************************************************/

    // Receptionist class
    class Receptionist implements Runnable
    {
        /*Semaphore to note that doctor is/isnt busy*/
        Semaphore receptionistSem = new Semaphore(1);
        Patient currentPatient;

        Receptionist(){
            Thread t = new Thread(this, "Doctor");
            t.setDaemon(true);
            t.start();
        }

        @Override
        public void run() {
            while(true){
                try{
                    produceReceptionistSem.acquire();
                    enteredSem.acquire(); //decrement entered sem, since checking someone in
                    currentPatient = enteredList.remove(0); //grab the patient we are checking in

                    Thread.sleep(TIMER);
                    System.out.println("Receptionist registers patient "+ currentPatient.PatientID);
                    currentPatient.seenReceptionist = true;
                    currentPatient.seenReceptionistSem.release(); //notify the nurse that this one has seen receptionist
                } catch(InterruptedException e) {
                    System.out.println("InterruptedException caught");
                }
                consumeReceptionistSem.release();
            }
        }
    }

    /**************************************************************************/
    /**************************************************************************/

    public static void main(String args[])
    {
        //set number of patients and doctors --NetBeans
        int p = 10;
        int d = 3;

        //set number of patients and doctors --Command line
//        int p = Integer.parseInt(args[0]);
//        int d = Integer.parseInt(args[1]);

        System.out.println("Run with "+ p+ " patients, "+ d+" nurses, "+ d+" doctors");
        System.out.println("");

        // Start a new doctors office instance
        DoctorsOffice doctorsOffice = new DoctorsOffice(p, d);

    }
}
