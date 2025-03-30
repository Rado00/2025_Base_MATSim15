module load stack/2024-06 
module load gcc/12.2.0
module load openjdk/17.0.8.1_1

jar_path="/cluster/home/kaghog/abmt2024/abmt2024/abmt2024/target/abmt2024-1.0-SNAPSHOT.jar"
config_path="/cluster/home/kaghog/abmt2024/ZurichScenario/zurich_25pctconfig_ih.xml"
run_path="org.eth.project.mode_choice.RunBaselineSimulation"
sbatch -n 1 --cpus-per-task=12 \
    --time=24:00:00 \
    --job-name="abmt2024" \
    --mem-per-cpu=1000 \
    --wrap="java -Xmx24G -cp ${jar_path} ${run_path} \
            --config-path ${config_path}
    "