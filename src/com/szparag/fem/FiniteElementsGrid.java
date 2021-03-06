package com.szparag.fem;

import java.util.LinkedList;

/**
 * Created by Ciemek on 20/01/16.
 */
public class FiniteElementsGrid {

    /**
     * list of elements inside the grid
     */
    private LinkedList<FiniteElement>   elements;
    private LinkedList<Node>            nodes;

    /**
     *  globally gathered matrixes from elements
     */
    private float[][]   kGlobalMatrix;
    private float[][]   fGlobalVector;
    private float[]     temperatures;


    public FiniteElementsGrid(LinkedList<FiniteElement> elements, LinkedList<Node> nodes, float radiusMax, float deltaRadius) {
        this.elements = elements;
        this.nodes = nodes;
    }



    /**
     * calculation methods
     */

    public void calculateLocalMatrixes(float radiusStart, float deltaRadius, float radiusMax, float deltaTime, float alpha) {
        float localRadiusStart = radiusStart;
        for (FiniteElement element : elements) {
            element.instantiateMatrix();
            element.calculateLocalMatrix(localRadiusStart, deltaRadius, deltaTime);
            if(elements.getLast() == element) {
                element.addBoundaryConditionsMatrix(alpha, radiusMax);
              //  System.out.println("BOUNDARY CONDITION APPLIED.");
            }

           // element.printMatrix();
            localRadiusStart +=deltaRadius;
        }
    }


    public void calculateLocalVectors(float radiusStart, float deltaRadius, float radiusMax, float deltaTime, float alpha, float temperatureAir) {

        float localRadiusStart = radiusStart;
        for (FiniteElement element : elements) {
            element.instantiateVector();
            element.calculateLocalVector(localRadiusStart, deltaRadius, deltaTime);
            if (elements.getLast() == element){
                element.addBoundaryConditionsVector(alpha, radiusMax, temperatureAir);
                System.out.print("BOUNDARY CONDITION APPLIED.");
            }

          //  element.printVector();
            localRadiusStart += deltaRadius;
        }
    }


    public void generateGlobalMatrix() {
        instantiateGlobalMatrix();

        for (int e =0; e < elements.size(); ++e) {

            kGlobalMatrix[e][e] += elements.get(e).getkLocalMatrix()[0][0];
            kGlobalMatrix[e][e+1] += elements.get(e).getkLocalMatrix()[0][1];
            kGlobalMatrix[e+1][e] += elements.get(e).getkLocalMatrix()[1][0];
            kGlobalMatrix[e+1][e+1] += elements.get(e).getkLocalMatrix()[1][1];
        }

     //   printMatrix();
    }


    public void generateGlobalVector() {
        instantiateGlobalVector();

        for (int e =0; e < elements.size(); ++e) {
            fGlobalVector[e][0] += elements.get(e).getfLocalVector()[0][0];
            fGlobalVector[e+1][0] += elements.get(e).getfLocalVector()[1][0];
        }

      //  printVector();

    }


    public void calculateTemperatures() {
        temperatures = new float[elements.size()+1];
        float[] tempValue = new float[elements.size()+1];
        int iterationCount = 100000;

        /**
         * calculation arrays instantiation
         */
        for (int i=0; i < elements.size()+1; ++i) {
            temperatures[i] = 0;
            tempValue[i] = 0;
        }


        /**
         * solving system of equations
         * Gauss-Siedel method
         */
        while(iterationCount > 0) {

            for (int i=0; i < elements.size()+1; ++i) {
                tempValue[i]=(fGlobalVector[i][0] / kGlobalMatrix[i][i]);
                for (int j=0; j < elements.size()+1; ++j) {
                    if (i == j) continue;
                    tempValue[i] = tempValue[i] - ((kGlobalMatrix[i][j] / kGlobalMatrix[i][i]) * temperatures[j]);
                    temperatures[i] = tempValue[i];
                }
            }

            --iterationCount;
        }

        for (int i=0; i < elements.size()+1; ++i) {
            nodes.get(i).setTemperature(temperatures[i]);
        }

        printTemperatures("TEMPERATURES:");

    }





    private void instantiateGlobalMatrix() {
        kGlobalMatrix = new float[elements.size()+1][elements.size()+1];

        for (float[] row: kGlobalMatrix)
            for (float element: row)
                element = 0;
    }

    private void instantiateGlobalVector() {
        fGlobalVector = new float[elements.size()+1][1];

        for (float[] row: fGlobalVector)
            for (float element: row)
                element = 0;
    }


    /**
     * printing methods
     */

    public void printMatrix() { print(kGlobalMatrix, "GLOBAL MATRIX:"); }

    public void printVector() { print(fGlobalVector, "GLOBAL VECTOR:"); }

    public void printTemperatures(String id) {
        System.out.println(id);
        System.out.print("|");

        for (Node node : nodes) System.out.print("[" + node.getTemperature() + "]");

        System.out.println("|");
        System.out.println(" ");
    }

    private void print(float[][] array, String id) {
        System.out.println(id);

        for(float[] row : array) {
            System.out.print("|");
            for (float element : row)
            if(element == 0) System.out.print("[ " + "00.0000" + " ]");
                else System.out.print("[ " + element + " ]");
            System.out.println("|");
        }

        System.out.println("endprint\n");
    }


    /**
     * accessors
     */

    public LinkedList<FiniteElement> getElements() {
        return elements;
    }

}
