# STD Projeto Loon Schaiana
Projeto desenvolvido na disciplina de Sistemas Distribuídos.
# Como usar:
Há três projetos: Cliente, Balão e ERB.

Podem ser executadas várias instâncias de cada projeto simultanemente.

Exemplo: executar as linhas abaixo em terminais distintos.
```
java -jar "Erb.jar" -p 5001 -lat -27.122430 -long 48.292900
```
```
java -jar "Balao.jar" -p 4100 -lat -27.122430 -long 48.292900
```
```
java -jar "Cliente.jar" -ip 127.0.0.1 -p 4100 ../payload.txt
```
# Exemplos:
## Exemplo 1:
O exemplo 1 mostra o envio de um arquivo com uma instância de Balão e uma instância de ERB.

No arquivo erbs.conf há três ERBs configuradas, porém, somente uma delas estava sendo executada.

O balão tentou conectar à instância de ERB mais próxima, porém, ela não estava sendo executada, então conectou à próxima.
![Image of Exemplo 1](https://github.com/STD29006-201702/projeto-01-schaiana/blob/master/Imagens/STD_Loon_Schaiana_Teste_1.png)

## Exemplo 2:
O exemplo 2 mostra o envio de um arquivo com duas instâncias de Balão e uma instância de ERB.

No arquivo erbs.conf há três ERBs configuradas, porém, somente uma delas estava sendo executada.

O balão 1 está a mais do que 40 km da ERB, então ele repassa a mensagem para o seu balão vizinho; o balão vizinho tentou conectar à instância de ERB mais próxima, porém, ela não estava sendo executada, então conectou à próxima.
![Image of Exemplo 2](https://github.com/STD29006-201702/projeto-01-schaiana/blob/master/Imagens/STD_Loon_Schaiana_Teste_2.png)
