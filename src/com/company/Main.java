package com.company;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    public static void main(String[] args) {
	    //correctnessTest();
        //System.out.println(factorial(12));
        //timeTest();
        solutionQuality();

    }
    public static void solutionQuality(){
        System.out.println("Solution Quality Ratio");
        System.out.format("%18s %18s %18s %18s",
                "N", "Brute Cost", "Greedy Cost", "SQR\n");
        int maxN = 13;
        double greedyCost = 0;
        double bruteCost = 0;

        for(int N = 4; N < maxN; N++) {
            //double[][] a = GenerateRandomCostMatrix(N,100);
            double[][] a = GenerateRandomEuclideanCostMatrix(N, 100);
            //double[][] a = GenerateRandomCircularGraphCostMatrix(40, 100, true);

            TSPGraph g1 = new TSPGraph(a);
            g1.Greedy();
            greedyCost = g1.getCost();

            TSPGraph g2 = new TSPGraph(a);
            g2.BruteWrapper();
            bruteCost = g2.getCost();


            System.out.format("%18s %18.2f %18.2f %18.2f\n",
                    N, bruteCost, greedyCost, greedyCost / bruteCost);

        }
    }
    public static void timeTest(){
        System.out.println("Greedy");
        System.out.format("%18s %18s %18s %18s",
                "N", "Time", "2x Ratio", "Expected 2x Ratio\n");

        int maxN = 999999;
        long[] times = new long[maxN];
        long start = 0;
        long stop = 0;

        for(int N = 50; N < maxN; N = N * 2) {
            double[][] a = GenerateRandomCostMatrix(N,100);
            //double[][] a = GenerateRandomEuclideanCostMatrix(5, 9);
            //double[][] a = GenerateRandomCircularGraphCostMatrix(40, 100, true);

            TSPGraph g = new TSPGraph(a);

            //start = getCpuTime();
            start = System.nanoTime();
            g.Greedy();

            //stop = getCpuTime();
            stop = System.nanoTime();

            times[N] = (stop - start) / 1000;
            System.out.format("%18s %18s %18.2f %18.2f\n",
                    N, times[N], (N > 50) ? (float)times[N]/times[N/2]: 0,  (N > 50) ? (float) Math.pow(N, 2) / Math.pow(N/2, 2) : 0);

        }
    }
    public static long factorial(long n){
        long result = 1;
        for(int i = 2; i <= n; i++){
            result = result * i;
        }
        return result;
    }
    public static void correctnessTest(){
        //double[][] a = GenerateRandomCostMatrix(5,9);
        //double[][] a = GenerateRandomEuclideanCostMatrix(5, 9);
        double[][] a = GenerateRandomCircularGraphCostMatrix(40, 100, true);

        TSPGraph g = new TSPGraph(a);
        g.Greedy();
        System.out.format("\nGreedy path: ");
        for( Integer e: g.path)
            System.out.format("%d ", e);
        System.out.format("\n");
        System.out.format("Greedy cost: ");
        System.out.println(g.getCost());
        System.out.format("\n");

        double[][] b = GenerateRandomCircularGraphCostMatrix(60, 100, true);

        TSPGraph h = new TSPGraph(b);
        h.Greedy();
        System.out.format("\nGreedy path: ");
        int q = 0;
        for( Integer e: h.path) {
            System.out.format("%d ", e);
            q++;
            if(q == 40) System.out.format("\n");
        }
        System.out.format("\n");
        System.out.format("Greedy cost: ");
        System.out.println(h.getCost());
    }
    public static class TSPGraph{
        double[][] costMatrix;
        ArrayList<Integer> path; // Lowest cost path
        ArrayList<Integer> perspectivePath; // Potential lowest cost path. Used to test minimum cost vs path

        public TSPGraph(double[][] a){
            costMatrix = a;
            path = new ArrayList<>(a.length);

        }
        public void Brute(ArrayList<Integer> usedNodes, ArrayList<Integer> unusedNodes){
            int tmpNode = 0;
            int length = unusedNodes.size();

            // Temp ArrayLists since Java Lists are references
            ArrayList<Integer> tmpUsedNodes = new ArrayList<>(usedNodes);
            ArrayList<Integer> tmpUnusedNodes = new ArrayList<>(unusedNodes);

            // If there are no more unused nodes, set the perspective path to the used nodes
            // and make the perspective path the new lowest cost path if either "path" is empty or
            // cost of the perspective path is lower than the current "path". Then return.
            if(unusedNodes.isEmpty()){
                perspectivePath = tmpUsedNodes;
                perspectivePath.add(0); // Add "0" to the end of the list
                if(path.isEmpty()|| getCost() > getPerspectiveCost() )
                    path = perspectivePath;
                return;
            }

            // Recursive section, which ensures all possible permutations of nodes are tested.
            // This for loop cycles through all possible next nodes to place in usedNodes. And recursively
            // calls Brute on each possibility.
            for(int i = 0; i < length; i++){
                tmpNode = tmpUnusedNodes.get(0);
                tmpUnusedNodes.remove(0); // Removes first item in unused list
                tmpUsedNodes.add(tmpNode); // Adds above removed item to used list

                Brute(tmpUsedNodes, tmpUnusedNodes); // Recursive call

                tmpUnusedNodes.add(tmpNode); // Adds removed item to end of unused list
                tmpUsedNodes.remove(tmpUsedNodes.size() - 1); // removes last item in used list
            }


        }

        public void BruteWrapper(){
            ArrayList<Integer> uN = new ArrayList<>();
            ArrayList<Integer> uuN = new ArrayList<>();
            uN.add(0);
            for(int w = 1; w < costMatrix.length; w++)
                uuN.add(w);
            Brute(uN, uuN);
        }
        public void Greedy(){
            int N = costMatrix.length;
            int[] travelLog = new int[N + 1];
            travelLog[0] = 1;
            for(int i = 0; i < N + 1; i++)
                path.add(0);

            for(int i = 1; i < N; i++){
                double low = -1;

                for(int j = 0; j < N; j++){
                    if( travelLog[j] != 1
                            && j != path.get(i-1)
                            && costMatrix[ path.get(i-1) ][j] != 0
                            && (low == -1 || costMatrix[ path.get(i-1) ][j] < low)){

                        low = costMatrix[ path.get(i-1) ][j];
                        path.set(i, j);
                    }
                }
                travelLog[path.get(i)] = 1;
            }
        }

        public double getCost(){
            double cost = 0;

            for(int i = 1; i < path.size(); i++){
                double tmpCost = costMatrix[ path.get(i - 1) ][ path.get(i) ];
                cost += (tmpCost != 0) ? tmpCost : 999999;
            }
            return cost;
        }
        public double getPerspectiveCost(){
            double cost = 0;

            for(int i = 1; i < perspectivePath.size(); i++){
                double tmpCost = costMatrix[ perspectivePath.get(i - 1) ][ perspectivePath.get(i) ];
                cost += (tmpCost != 0) ? tmpCost : 999999;
            }
            return cost;
        }
    }

    public static int[] TSPGreedy(double[][] costMatrix){
        int N = costMatrix.length;
        int[] path = new int[N + 1]; //


        for(int i = 1; i < N; i++){
            double low = -1;

            for(int j = 0; j < N; j++){
                if( !contains(path, j)
                    && j != path[i-1]
                    && costMatrix[ path[i-1] ][j] != 0
                    && (low == -1 || costMatrix[ path[i-1] ][j] < low)){

                    low = costMatrix[ path[i-1] ][j];
                    path[i] = j;
                }
            }



        }

        return path;
    }

    public static boolean contains(int[] a, int b){
        boolean hasValue = false;

        for(int i = 0; i < a.length; i++){
            if(a[i] == b){
                hasValue = true;
                break;
            }
        }
        return hasValue;

    }

    public static double[][] GenerateRandomCircularGraphCostMatrix(int N, double r, boolean isTest){
        double[][] costMatrix = new double[N][N];
        double[][] graphPoints = new double[N][2];
        int[] path = new int[N + 1];

        double pi = Math.PI;
        graphPoints[0][0] = 0;
        graphPoints[0][1] = r;

        ArrayList<Integer> nodeSet = new ArrayList<>();
        for(int i = 1; i < N; i++)
            nodeSet.add(i);

        for(int i = 1; i < N; i++){
            double x = r * Math.sin(2 * pi / N * i);
            double y = r * Math.cos(2 * pi / N * i);

            int randomNum =  ThreadLocalRandom.current().nextInt(0, N - i );
            int a = nodeSet.get(randomNum);
            nodeSet.remove(randomNum);

            graphPoints[a][0] = x;
            graphPoints[a][1] = y;
            path[i] = a;
        }
        path[path.length - 1] = 0;

        double distance = -1;

        for(int j = 0; j < N - 1; j++){
            for(int k = N - 1; k > j; k--){
                double x1 = graphPoints[j][0];
                double y1 = graphPoints[j][1];
                double x2 = graphPoints[k][0];
                double y2 = graphPoints[k][1];
                double cost = Math.sqrt( Math.pow( (x2 - x1), 2 ) + Math.pow( (y2 - y1), 2 ) );

                costMatrix[j][k] = cost;
                costMatrix[k][j] = cost;
                if(distance == -1 || cost < distance)
                    distance = cost;
            }
        }

        if(isTest){
            for(int j = 0; j < N + 1; j++){
                if(j % 5 == 0 && j > 0) System.out.format("\n");
                System.out.format("%d (%.2f, %.2f)  ", path[j], graphPoints[ path[j] ][0], graphPoints[ path[j] ][1]);
            }
            double expectedCost = N * distance;
            System.out.format("\nExpected Cost: %.2f\n", expectedCost);
            System.out.format("\n");
            for(int i = 0; i < N; i++){
                for(int j = 0; j < N; j++){
                    //System.out.format("%6.2f ", costMatrix[i][j]);
                }
                //System.out.format("\n");
            }
        }

        return costMatrix;
    }

    public static double[][] GenerateRandomEuclideanCostMatrix(int N, int max){
        int[][] graphPoints = new int[N][2]; // holds x/y coordinates for vertices
        int randomNum;
        double[][] costMatrix = new double[N][N];

        for(int i = 0; i < N; i++){
            randomNum =  ThreadLocalRandom.current().nextInt(0, max + 1);
            graphPoints[i][0] = randomNum;
            randomNum =  ThreadLocalRandom.current().nextInt(0, max + 1);
            graphPoints[i][1] = randomNum;
        }

        for(int j = 0; j < N - 1; j++){
            for(int k = N - 1; k > j; k--){
                int x1 = graphPoints[j][0];
                int y1 = graphPoints[j][1];
                int x2 = graphPoints[k][0];
                int y2 = graphPoints[k][1];
                double cost = Math.sqrt( Math.pow( (x2 - x1), 2 ) + Math.pow( (y2 - y1), 2 ) );

                costMatrix[j][k] = cost;
                costMatrix[k][j] = cost;
            }
        }
        return costMatrix;
    }

    public static double[][] GenerateRandomCostMatrix(int N, int maxVal){
        double[][] costMatrix = new double[N][N];
        int randomNum;

        for(int i = 0; i < N; i++){
            for(int j = i; j < N; j++){
                if(i == j){
                    costMatrix[i][j] = 0;
                }
                else{
                    //Used top comment for random number generator https://stackoverflow.com/questions/363681/how-do-i-generate-random-integers-within-a-specific-range-in-java
                    randomNum =  ThreadLocalRandom.current().nextInt(1, maxVal + 1);
                    costMatrix[i][j] = randomNum;
                    costMatrix[j][i] = randomNum;
                }
            }
        }

        return costMatrix;
    }
}
