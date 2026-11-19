import { useEffect, useState } from 'react'
import { ApiError, listarHistorico, type FiltroHistorico, type Movimentacao, type Operacao, type StatusMovimentacao } from '@/api'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { formatarDataHora, formatarQuantidade, nomeTipo } from '@/format'

type HistoricoPainelProps = {
  versao: number
}

const filtroInicial: FiltroHistorico = {
  operacao: '',
  status: '',
  order: 'desc',
}

export function HistoricoPainel({ versao }: HistoricoPainelProps) {
  const [filtro, setFiltro] = useState<FiltroHistorico>(filtroInicial)
  const [movimentos, setMovimentos] = useState<Movimentacao[]>([])
  const [erro, setErro] = useState<string | null>(null)

  useEffect(() => {
    let ativo = true
    listarHistorico(filtro)
      .then((lista) => {
        if (ativo) {
          setMovimentos(lista)
          setErro(null)
        }
      })
      .catch((causa: unknown) => {
        if (ativo) {
          setErro(causa instanceof ApiError ? causa.message : 'Não foi possível carregar o histórico.')
        }
      })
    return () => {
      ativo = false
    }
  }, [filtro, versao])

  return (
    <Card>
      <CardHeader>
        <CardTitle>Histórico</CardTitle>
        <CardDescription>Entradas, saídas e tentativas recusadas, da mais recente para a mais antiga.</CardDescription>
      </CardHeader>
      <CardContent className="grid gap-4">
        <div className="grid gap-3 sm:grid-cols-3">
          <Filtro
            label="Operação"
            value={filtro.operacao || 'todas'}
            onChange={(valor) =>
              setFiltro((atual) => ({ ...atual, operacao: valor === 'todas' ? '' : (valor as Operacao) }))
            }
            opcoes={[
              ['todas', 'Todas'],
              ['ENTRADA', 'Entradas'],
              ['SAIDA', 'Saídas'],
            ]}
          />
          <Filtro
            label="Status"
            value={filtro.status || 'todos'}
            onChange={(valor) =>
              setFiltro((atual) => ({
                ...atual,
                status: valor === 'todos' ? '' : (valor as StatusMovimentacao),
              }))
            }
            opcoes={[
              ['todos', 'Todos'],
              ['SUCESSO', 'Concluídas'],
              ['FALHA', 'Recusadas'],
            ]}
          />
          <Filtro
            label="Ordem"
            value={filtro.order}
            onChange={(valor) => setFiltro((atual) => ({ ...atual, order: valor as 'asc' | 'desc' }))}
            opcoes={[
              ['desc', 'Mais recentes'],
              ['asc', 'Mais antigas'],
            ]}
          />
        </div>
        {erro ? <p className="text-sm text-destructive">{erro}</p> : null}
        {movimentos.length === 0 && !erro ? (
          <p className="text-sm text-muted-foreground">Nenhuma movimentação com esses filtros.</p>
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Quando</TableHead>
                <TableHead>Operação</TableHead>
                <TableHead>Ingrediente</TableHead>
                <TableHead>Quantidade</TableHead>
                <TableHead>Local</TableHead>
                <TableHead>Responsável</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {movimentos.map((item) => (
                <TableRow key={item.id}>
                  <TableCell className="whitespace-nowrap">{formatarDataHora(item.dataHora)}</TableCell>
                  <TableCell>
                    <div className="flex flex-col items-start gap-1">
                      <Badge variant={item.operacao === 'ENTRADA' ? 'secondary' : 'outline'}>
                        {item.operacao === 'ENTRADA' ? 'Entrada' : 'Saída'}
                      </Badge>
                      <Badge variant={item.status === 'SUCESSO' ? 'outline' : 'destructive'}>
                        {item.status === 'SUCESSO' ? 'Concluída' : 'Recusada'}
                      </Badge>
                    </div>
                  </TableCell>
                  <TableCell>
                    <div className="font-medium">{item.ingredienteNome ?? '—'}</div>
                    <div className="text-muted-foreground">{nomeTipo(item.tipo)}</div>
                    {item.metadata ? <div className="mt-1 max-w-64 text-xs text-muted-foreground">{item.metadata}</div> : null}
                  </TableCell>
                  <TableCell className="whitespace-nowrap">{formatarQuantidade(item.quantidade, item.tipo)}</TableCell>
                  <TableCell>{item.compartimentoCodigo ?? '—'}</TableCell>
                  <TableCell>{item.responsavel}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </CardContent>
    </Card>
  )
}

function Filtro({
  label,
  value,
  onChange,
  opcoes,
}: {
  label: string
  value: string
  onChange: (valor: string) => void
  opcoes: [string, string][]
}) {
  return (
    <div className="grid gap-2">
      <span className="text-sm font-medium">{label}</span>
      <Select value={value} onValueChange={onChange}>
        <SelectTrigger className="w-full">
          <SelectValue />
        </SelectTrigger>
        <SelectContent position="popper">
          {opcoes.map(([valor, texto]) => (
            <SelectItem key={valor} value={valor}>
              {texto}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  )
}
