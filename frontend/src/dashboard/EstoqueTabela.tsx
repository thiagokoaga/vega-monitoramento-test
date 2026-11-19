import type { Ingrediente } from '@/api'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { formatarQuantidade, nomeTipo } from '@/format'

type EstoqueTabelaProps = {
  ingredientes: Ingrediente[]
  onUsarNaSaida: (id: string) => void
}

export function EstoqueTabela({ ingredientes, onUsarNaSaida }: EstoqueTabelaProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Estoque</CardTitle>
        <CardDescription>
          {ingredientes.length === 0
            ? 'Nenhum ingrediente cadastrado.'
            : `${ingredientes.length} ${ingredientes.length === 1 ? 'ingrediente' : 'ingredientes'} com saldo atual.`}
        </CardDescription>
      </CardHeader>
      <CardContent>
        {ingredientes.length > 0 ? (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Ingrediente</TableHead>
                <TableHead>Tipo</TableHead>
                <TableHead>Saldo</TableHead>
                <TableHead>Compartimento</TableHead>
                <TableHead />
              </TableRow>
            </TableHeader>
            <TableBody>
              {ingredientes.map((item) => (
                <TableRow key={item.id}>
                  <TableCell className="font-medium">{item.nome}</TableCell>
                  <TableCell>
                    <Badge variant="secondary">{nomeTipo(item.tipo)}</Badge>
                  </TableCell>
                  <TableCell>{formatarQuantidade(item.quantidade, item.tipo)}</TableCell>
                  <TableCell>{item.compartimentoCodigo}</TableCell>
                  <TableCell className="text-right">
                    <Button type="button" variant="ghost" size="sm" onClick={() => onUsarNaSaida(String(item.id))}>
                      Usar na saída
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        ) : null}
      </CardContent>
    </Card>
  )
}
