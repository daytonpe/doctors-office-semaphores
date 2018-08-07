# Doctor's Office Model Using Java Threads

## Overview

The clinic to be simulated has doctors, each of which has their own nurse.  Each doctor has an office of his or her own in which to visit patients.  Patients will enter the clinic to see a doctor, which should be randomly assigned.  Initially, a patient enters the waiting room and waits to register with the receptionist.  Once registered, the patient sits in the waiting room until the nurse calls.  The receptionist lets the nurse know a patient is waiting.  The nurse directs the patient to the doctor’s office and tells the doctor that a patient is waiting.  The doctor visits the patient and listens to the patient’s symptoms.  The doctor advises the patient on the action to take.  The patient then leaves.


## Setup
```
$cd doctorsoffice
$javac Project2.java
$cd ..
$java -cp . doctorsoffice.DoctorsOffice 5 3
```

## Threads
* Receptionist – one thread
* Doctor – one thread each, maximum of 3 doctors
* Nurse – one per doctor thread, identifier of doctor and corresponding nurse should match
* Patient – one thread each, up to 30 patients

## Inputs
The program should receive the number of doctors and patients as command-line inputs.  

## Other Rules
* A thread should sleep 1 second at each step the thread prints an activity.  
* All mutual exclusion and coordination must be achieved with semaphores.  
* A thread may not use sleeping as a means of coordination.  
* Busy waiting (polling) is not allowed.
* Mutual exclusion should be kept to a minimum to allow the most concurrency.
* Each thread should only print its own activities.  The patient threads prints patient actions and the doctor threads prints doctor actions, etc.
* Your output must include the same information and the same set of steps as the sample output.

## Example Output:

Run with 5 patients, 3 nurses, 3 doctors
```
Patient 1 enters waiting room, waits for receptionist
Receptionist registers patient 1
Nurse 1 takes patient 1 to doctor's office
Patient 2 enters waiting room, waits for receptionist
Receptionist registers patient 2
Doctor 1 listens to symptoms from patient 1
Nurse 2 takes patient 2 to doctor's office
Patient 3 enters waiting room, waits for receptionist
Patient 1 receives advice from doctor 1
Receptionist registers patient 3
Nurse 3 takes patient 3 to doctor's office
Doctor 2 listens to symptoms from patient 2
Patient 4 enters waiting room, waits for receptionist
Patient 1 leaves
Doctor 3 listens to symptoms from patient 3
Patient 2 receives advice from doctor 2
Nurse 1 takes patient 4 to doctor's office
Receptionist registers patient 4
Patient 5 enters waiting room, waits for receptionist
Patient 2 leaves
Receptionist registers patient 5
Nurse 2 takes patient 5 to doctor's office
Doctor 1 listens to symptoms from patient 4
Patient 3 receives advice from doctor 3
Patient 4 receives advice from doctor 1
Doctor 2 listens to symptoms from patient 5
Patient 3 leaves
Patient 4 leaves
Patient 5 receives advice from doctor 2
Patient 5 leaves
```
