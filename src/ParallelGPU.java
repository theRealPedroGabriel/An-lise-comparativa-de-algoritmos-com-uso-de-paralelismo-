import org.jocl.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.jocl.CL.*;

public class ParallelGPU {

    private static String programSource =
            "__kernel void countCharacters(__global const char *data, __global int *results, const char toFind) {" +
                    "    int gid = get_global_id(0);" +
                    "    results[gid] = (data[gid] == toFind) ? 1 : 0;" +
                    "}";

    public static void main(String[] args) {
        String[] fileNames = {
                "C:\\Users\\pgsmc\\OneDrive\\Documentos\\EstruturadeDados\\carrinho-de-compras\\projeto final paralelo concorrente\\src\\DonQuixote-388208.txt",   //grande
                "C:\\Users\\pgsmc\\OneDrive\\Documentos\\EstruturadeDados\\carrinho-de-compras\\projeto final paralelo concorrente\\src\\MobyDick-217452.txt",  //medio
                "C:\\Users\\pgsmc\\OneDrive\\Documentos\\EstruturadeDados\\carrinho-de-compras\\projeto final paralelo concorrente\\src\\Dracula-165307.txt"    //pequeno
        };
        char toFind = 'e'; // Caractere a ser encontrado

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("results.csv", true))) {
            // Verifica se o arquivo já existe e se é necessário escrever o cabeçalho
            File file = new File("results.csv");
            if (file.length() == 0) {
                writer.write("Filename,Occurrences,Execution Time (ms)\n");
            }

            // Inicializa OpenCL
            cl_platform_id[] platforms = new cl_platform_id[1];
            clGetPlatformIDs(platforms.length, platforms, null);
            cl_device_id[] devices = new cl_device_id[1];
            clGetDeviceIDs(platforms[0], CL_DEVICE_TYPE_GPU, devices.length, devices, null);
            cl_context context = clCreateContext(null, 1, devices, null, null, null);
            cl_command_queue queue = clCreateCommandQueue(context, devices[0], 0, null);

            cl_program program = clCreateProgramWithSource(context, 1, new String[]{programSource}, null, null);
            clBuildProgram(program, 0, null, null, null, null);
            cl_kernel kernel = clCreateKernel(program, "countCharacters", null);

            for (String fileName : fileNames) {
                String text = Files.readString(Paths.get(fileName));
                long numBytes = text.length();
                Pointer src = Pointer.to(text.toCharArray());
                Pointer out = Pointer.to(new int[text.length()]);

                cl_mem srcMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_char * numBytes, src, null);
                cl_mem outMem = clCreateBuffer(context, CL_MEM_WRITE_ONLY, Sizeof.cl_int * numBytes, null, null);

                clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(srcMem));
                clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(outMem));
                clSetKernelArg(kernel, 2, Sizeof.cl_char, Pointer.to(new char[]{toFind}));

                long startTime = System.nanoTime();
                clEnqueueNDRangeKernel(queue, kernel, 1, null, new long[]{numBytes}, null, 0, null, null);
                clFinish(queue);
                long endTime = System.nanoTime();

                int[] results = new int[text.length()];
                clEnqueueReadBuffer(queue, outMem, CL_TRUE, 0, numBytes * Sizeof.cl_int, Pointer.to(results), 0, null, null);

                int total = 0;
                for (int result : results) {
                    total += result;
                }
                long duration = (endTime - startTime) / 1000000; // Convert to milliseconds
                System.out.println("Occurrences of '" + toFind + "' in " + fileName + ": " + total + ", Execution Time: " + duration + " ms");

                // Escreve os resultados no arquivo CSV
                writer.write(fileName + "," + total + "," + duration + "\n");

                clReleaseMemObject(srcMem);
                clReleaseMemObject(outMem);
            }

            clReleaseKernel(kernel);
            clReleaseProgram(program);
            clReleaseCommandQueue(queue);
            clReleaseContext(context);

        } catch (IOException e) {
            System.err.println("Error reading file or writing to CSV: " + e.getMessage());
        }
    }
}
